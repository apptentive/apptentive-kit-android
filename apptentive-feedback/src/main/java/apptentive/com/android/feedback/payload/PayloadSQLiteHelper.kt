package apptentive.com.android.feedback.payload

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.VisibleForTesting
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.encryption.EncryptionNoOp
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.payload.EncryptedPayloadTokenUpdater.Companion.updateEmbeddedToken
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.SDKState
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.isMarshmallowOrGreater
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.LogTags.PAYLOADS
import apptentive.com.android.util.isNotNullOrEmpty
import java.io.FileNotFoundException
import java.io.IOException

internal class PayloadSQLiteHelper(val context: Context, val encryption: Encryption) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_QUERY_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_QUERY_DROP_TABLE)
        onCreate(db)
    }

    fun addPayload(payload: PayloadData) {
        Log.v(PAYLOADS, "Saving payload body to: ${writableDatabase.path}")
        val fileName = if (payload.sidecarData.data.isNotEmpty()) {
            val encryptedBytes = determineEncryption().encrypt(payload.sidecarData.data)
            val fileName = FileUtil.generateCacheFilePathFromNonceOrPrefix(context, payload.nonce, "apptentive-message-payload")
            FileUtil.writeFileData(fileName, encryptedBytes)
            fileName
        } else ""
        val values = ContentValues().apply {
            put(COL_NONCE, payload.nonce)
            put(COL_TYPE, payload.type.toString())
            put(COL_PATH, payload.path)
            put(COL_METHOD, payload.method.toString())
            put(COL_MEDIA_TYPE, payload.mediaType.toString())
            put(COL_PAYLOAD_DATA, determineEncryption().encrypt(payload.data))
            put(COL_PAYLOAD_DATA_FILE, fileName)
            put(COL_TAG, payload.tag)

            payload.token?.takeIf { it.isNotNullOrEmpty() }?.let {
                put(COL_TOKEN, it)
            }

            payload.conversationId?.takeIf { it.isNotNullOrEmpty() }?.let {
                put(COL_CONVERSATION_ID, it)
            }

            put(COL_ENCRYPTED, if (payload.isEncrypted) 1 else 0)
        }

        try {
            synchronized(this) {
                writableDatabase.use { db ->
                    val result = db.insert(TABLE_NAME, null, values)
                    if (result == -1L) {
                        throw RuntimeException("Unable to add payload: $payload")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(PAYLOADS, "Error writing to database", e)
        }
    }

    fun deletePayload(nonce: String): Boolean {
        synchronized(this) {
            writableDatabase.use { db ->
                deletePayload(db, nonce)
            }
        }
        return false
    }

    fun nextUnsentPayload(): PayloadData? {
        synchronized(this) {
            writableDatabase.use { db ->
                while (true) {
                    db.select(tableName = TABLE_NAME, where = "$COL_TOKEN IS NOT NULL", orderBy = COL_PRIMARY_KEY, limit = 1)
                        .use { cursor ->
                            if (cursor.moveToFirst()) {
                                try {
                                    return readPayload(cursor)
                                } catch (e: Exception) {
                                    val nonce = cursor.getString(COL_NONCE)
                                    Log.e(
                                        PAYLOADS,
                                        "Exception reading payload. Unable to send. Deleting.",
                                        e
                                    )
                                    deletePayload(db, nonce)
                                }
                            } else {
                                return null
                            }
                        }
                }
            }
        }
    }

    private fun determineEncryption(): Encryption =
        if (DefaultStateMachine.state == SDKState.LOGGED_IN || DefaultStateMachine.state == SDKState.LOGGED_OUT) EncryptionNoOp() // For Logged in conversation using the encryptionKey from ConversationCredential
        else encryption

    private fun deletePayload(db: SQLiteDatabase, nonce: String): Boolean {
        val deletedRows = db.delete(TABLE_NAME, column = COL_NONCE, value = nonce)
        return deletedRows > 0
    }

    internal fun updateCredential(credentialProvider: ConversationCredentialProvider): Boolean {
        val token = credentialProvider.conversationToken
        val conversationId = credentialProvider.conversationId
        val tag = credentialProvider.conversationPath
        val encryptionKey = credentialProvider.payloadEncryptionKey

        if (token != null && conversationId != null) {
            synchronized(this) {
                writableDatabase.use { db ->
                    Log.d(PAYLOADS, "Updating credentials for payloads with tag $tag")

                    // Grab each payload with the specified tag and update as needed.
                    db.select(tableName = TABLE_NAME, where = "$COL_TAG = ?", selectionArgs = arrayOf(tag), orderBy = COL_PRIMARY_KEY)
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                val payloadData = readPayload(cursor)
                                val contentValues = ContentValues()

                                // May not have had conversation ID when enqueued.
                                contentValues.put(COL_CONVERSATION_ID, conversationId)

                                if (payloadData.isEncrypted) {
                                    if (isMarshmallowOrGreater() && encryptionKey != null) {
                                        // If encrypted, we have to update the token the hard way.
                                        val updatedData = updateEmbeddedToken(
                                            token,
                                            encryptionKey,
                                            payloadData.type,
                                            payloadData.mediaType,
                                            payloadData.data
                                        )

                                        val encryptedBytes =
                                            determineEncryption().encrypt(updatedData)

                                        if (payloadData.sidecarData.dataFilePath.isNotNullOrEmpty()) {
                                            FileUtil.writeFileData(
                                                payloadData.sidecarData.dataFilePath,
                                                encryptedBytes
                                            )
                                        } else {
                                            contentValues.put(COL_PAYLOAD_DATA, encryptedBytes)
                                        }

                                        // Use placeholder for token column to indicate ready to send.
                                        contentValues.put(COL_TOKEN, "embedded")
                                    } else {
                                        Log.w(PAYLOADS, "Invalid encrypted payload when updating token.")
                                    }
                                } else {
                                    // For unencrypted, just update the token column with the value.
                                    contentValues.put(COL_TOKEN, token)
                                }

                                Log.d(PAYLOADS, "Updating credential for payload ${payloadData.nonce} with tag $tag, conversationId $conversationId")
                                db.update(TABLE_NAME, contentValues, "$COL_NONCE = ?", arrayOf(payloadData.nonce))
                            }
                        }
                }
            }

            return true
        } else {
            Log.w(LogTags.PAYLOADS, "Attempting to update payloads with invalid credentials.")
            return false
        }
    }

    fun invalidateCredential(tag: String) {
        writableDatabase.use { db ->
            val contentValues = ContentValues()
            contentValues.putNull(COL_TOKEN.toString())
            db.update(TABLE_NAME, contentValues, "$COL_TAG = ?", arrayOf(tag))
        }
    }

    internal fun deleteAllCachedPayloads() {
        writableDatabase.use { db ->
            db.execSQL("delete from $TABLE_NAME")
        }

        Log.w(LogTags.CRYPTOGRAPHY, "Payload cache is deleted to support the new encryption setting")
    }

    internal fun readPayloads(): List<PayloadData> {
        synchronized(this) {
            readableDatabase.use { db ->
                db.select(tableName = TABLE_NAME, orderBy = COL_PRIMARY_KEY)
                    .use { cursor ->
                        val result = mutableListOf<PayloadData>()
                        while (cursor.moveToNext()) {
                            result.add(readPayload(cursor))
                        }
                        return result
                    }
            }
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    private fun readPayload(cursor: Cursor): PayloadData {
        val dataBytes = determineEncryption().decrypt(cursor.getBlob(COL_PAYLOAD_DATA))
        val dataPath = cursor.getString(COL_PAYLOAD_DATA_FILE)
        val payloadData = if (dataBytes.isNotEmpty()) dataBytes
        else determineEncryption().decrypt(FileUtil.readFileData(dataPath))

        return PayloadData(
            nonce = cursor.getString(COL_NONCE),
            type = PayloadType.parse(cursor.getString(COL_TYPE)),
            tag = cursor.getString(COL_TAG),
            token = cursor.getString(COL_TOKEN),
            conversationId = cursor.getString(COL_CONVERSATION_ID),
            isEncrypted = cursor.getInt(COL_ENCRYPTED) == 1,
            path = cursor.getString(COL_PATH),
            method = HttpMethod.valueOf(cursor.getString(COL_METHOD)),
            mediaType = MediaType.parse(cursor.getString(COL_MEDIA_TYPE)),
            data = payloadData,
            sidecarData = SidecarData(dataFilePath = dataPath)
        )
    }

    @VisibleForTesting
    internal fun deleteDatabase(context: Context): Boolean {
        val file = context.getDatabasePath(DATABASE_NAME)
        return file.delete()
    }

    @VisibleForTesting
    internal fun updatePayload(nonce: String, payloadType: String) {
        synchronized(this) {
            writableDatabase.use { db ->
                val values = ContentValues().apply {
                    put(COL_TYPE, payloadType)
                }

                val selection = "$COL_NONCE = ?"
                val selectionArgs = arrayOf(nonce)
                val count = db.update(
                    TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
                )

                if (count == -1) {
                    throw RuntimeException("Unable to update payload")
                }
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "payloads.db"
        private const val DATABASE_VERSION = 3
        const val TABLE_NAME = "payloads"
        private val COL_PRIMARY_KEY = Column(index = 0, name = "_ID")
        private val COL_NONCE = Column(index = 1, name = "nonce")
        private val COL_TYPE = Column(index = 2, name = "payload_type")
        private val COL_PATH = Column(index = 3, name = "path")
        private val COL_METHOD = Column(index = 4, name = "method")
        private val COL_MEDIA_TYPE = Column(index = 5, name = "media_type")
        private val COL_PAYLOAD_DATA = Column(index = 6, name = "data")
        private val COL_PAYLOAD_DATA_FILE = Column(index = 7, name = "data_file")
        private val COL_TAG = Column(index = 8, name = "tag")
        private val COL_TOKEN = Column(index = 9, name = "token")
        private val COL_CONVERSATION_ID = Column(index = 10, name = "conversation_id")
        private val COL_ENCRYPTED = Column(index = 11, name = "encrypted")

        private val SQL_QUERY_CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
            "$COL_PRIMARY_KEY INTEGER PRIMARY KEY, " +
            "$COL_NONCE TEXT, " +
            "$COL_TYPE TEXT, " +
            "$COL_PATH TEXT, " +
            "$COL_METHOD TEXT, " +
            "$COL_MEDIA_TYPE TEXT, " +
            "$COL_PAYLOAD_DATA BLOB, " +
            "$COL_PAYLOAD_DATA_FILE TEXT," +
            "$COL_TAG TEXT," +
            "$COL_TOKEN TEXT," +
            "$COL_CONVERSATION_ID TEXT," +
            "$COL_ENCRYPTED INTEGER" +
            ")"

        private const val SQL_QUERY_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    private fun doesColumnExist(db: SQLiteDatabase, column: Column): Boolean {
        val columns = db.rawQuery("PRAGMA table_info($TABLE_NAME)", null)

        // Check if the column 'new_column' exists
        val columnIndex = columns.getColumnIndex("name")
        var columnExists = false

        while (columns.moveToNext()) {
            if (columns.getString(columnIndex) == column.toString()) {
                columnExists = true
                break
            }
        }

        columns.close()

        return columnExists
    }
}

private data class Column(val index: Int, val name: String) {
    override fun toString() = name
}

private fun SQLiteDatabase.select(tableName: String, where: String? = null, selectionArgs: Array<String>? = null, orderBy: Column, limit: Int? = null): Cursor {
    return query(
        tableName,
        null,
        where,
        selectionArgs,
        null,
        null,
        "${orderBy.name} ASC",
        limit?.toString()
    )
}

private fun SQLiteDatabase.delete(tableName: String, column: Column, value: String) =
    delete(tableName, "${column.name} = ?", arrayOf(value))

private fun ContentValues.put(column: Column, value: String) = put(column.name, value)
private fun ContentValues.put(column: Column, value: Int) = put(column.name, value)
private fun ContentValues.put(column: Column, value: ByteArray) = put(column.name, value)

private fun Cursor.getString(column: Column) = getString(column.index)
private fun Cursor.getInt(column: Column) = getInt(column.index)
private fun Cursor.getBlob(column: Column) = getBlob(column.index)
