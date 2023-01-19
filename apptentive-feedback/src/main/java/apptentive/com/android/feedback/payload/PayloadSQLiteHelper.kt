package apptentive.com.android.feedback.payload

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.VisibleForTesting
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.LogTags.PAYLOADS
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
        val fileName = if (payload.attachmentData.data.isNotEmpty()) {
            val encryptedBytes = encryption.encrypt(payload.attachmentData.data)
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
            put(COL_PAYLOAD_DATA, encryption.encrypt(payload.data))
            put(COL_PAYLOAD_DATA_FILE, fileName)
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
                    db.select(tableName = TABLE_NAME, orderBy = COL_PRIMARY_KEY, limit = 1)
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

    private fun deletePayload(db: SQLiteDatabase, nonce: String): Boolean {
        val deletedRows = db.delete(TABLE_NAME, column = COL_NONCE, value = nonce)
        return deletedRows > 0
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
        val dataBytes = encryption.decrypt(cursor.getBlob(COL_PAYLOAD_DATA))
        val dataPath = cursor.getString(COL_PAYLOAD_DATA_FILE)
        val payloadData = if (dataBytes.isNotEmpty()) dataBytes
        else encryption.decrypt(FileUtil.readFileData(dataPath))

        return PayloadData(
            nonce = cursor.getString(COL_NONCE),
            type = PayloadType.parse(cursor.getString(COL_TYPE)),
            path = cursor.getString(COL_PATH),
            method = HttpMethod.valueOf(cursor.getString(COL_METHOD)),
            mediaType = MediaType.parse(cursor.getString(COL_MEDIA_TYPE)),
            data = payloadData,
            attachmentData = AttachmentData(dataFilePath = dataPath)
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
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "payloads"
        private val COL_PRIMARY_KEY = Column(index = 0, name = "_ID")
        private val COL_NONCE = Column(index = 1, name = "nonce")
        private val COL_TYPE = Column(index = 2, name = "payload_type")
        private val COL_PATH = Column(index = 3, name = "path")
        private val COL_METHOD = Column(index = 4, name = "method")
        private val COL_MEDIA_TYPE = Column(index = 5, name = "media_type")
        private val COL_PAYLOAD_DATA = Column(index = 6, name = "data")
        private val COL_PAYLOAD_DATA_FILE = Column(index = 7, name = "data_file")

        private val SQL_QUERY_CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
            "$COL_PRIMARY_KEY INTEGER PRIMARY KEY, " +
            "$COL_NONCE TEXT, " +
            "$COL_TYPE TEXT, " +
            "$COL_PATH TEXT, " +
            "$COL_METHOD TEXT, " +
            "$COL_MEDIA_TYPE TEXT, " +
            "$COL_PAYLOAD_DATA BLOB, " +
            "$COL_PAYLOAD_DATA_FILE TEXT" +
            ")"
        private const val SQL_QUERY_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}

private data class Column(val index: Int, val name: String) {
    override fun toString() = name
}

private fun SQLiteDatabase.select(tableName: String, orderBy: Column, limit: Int? = null): Cursor {
    return query(
        tableName,
        null,
        null,
        null,
        null,
        null,
        "${orderBy.name} ASC",
        limit?.toString()
    )
}

private fun SQLiteDatabase.delete(tableName: String, column: Column, value: String) =
    delete(tableName, "${column.name} = ?", arrayOf(value))

private fun ContentValues.put(column: Column, value: String) = put(column.name, value)
private fun ContentValues.put(column: Column, value: ByteArray) = put(column.name, value)

private fun Cursor.getString(column: Column) = getString(column.index)
private fun Cursor.getBlob(column: Column) = getBlob(column.index)
