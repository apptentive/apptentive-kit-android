package apptentive.com.android.feedback.payload

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/*
interface PayloadMetadataStore {
    fun addMetadata(payload: PayloadMetadata)
    fun deleteMetadata(nonce: String): Boolean
    fun nextUnsentPayloadMetadata(): PayloadMetadata?
}
*/

// FIXME: provide a name for the helper (based on local conversation id)
// TODO: PayloadMetadataSQLiteStore?
class PayloadSQLiteHelper(context: Context) : /* implement PayloadMetadataStore */
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_QUERY_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_QUERY_DROP_TABLE)
        onCreate(db)
    }

    fun addPayload(payload: PayloadMetadata) {
        val values = ContentValues().apply {
            put(COL_NONCE, payload.nonce)
            put(COL_TYPE, payload.type)
            put(COL_MEDIA_TYPE, payload.mediaType)
        }

        writableDatabase.use { db ->
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun deletePayload(nonce: String): Boolean {
        writableDatabase.use { db ->
            val query = createPayloadDeleteQuery(nonce)
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    return true
                }
            }
        }
        return false

    }

    fun nextUnsentPayload(): PayloadMetadata? {
        writableDatabase.use { db ->
            db.rawQuery(SQL_QUERY_NEXT_UNSENT_PAYLOAD, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    return PayloadMetadata(
                        nonce = cursor.getString(COL_NONCE),
                        type = cursor.getString(COL_TYPE),
                        mediaType = cursor.getString(COL_MEDIA_TYPE)
                    )
                }

                return null
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "payload_db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "payloads"
        private val COL_PRIMARY_KEY = Column(index = 0, name = "_id")
        private val COL_NONCE = Column(index = 1, name = "nonce")
        private val COL_TYPE = Column(index = 2, name = "payload_type")
        private val COL_MEDIA_TYPE = Column(index = 3, name = "media_type")

        private val SQL_QUERY_CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$COL_PRIMARY_KEY INTEGER PRIMARY KEY, " +
                "$COL_NONCE TEXT, " +
                "$COL_TYPE TEXT, " +
                "$COL_MEDIA_TYPE TEXT" +
                ")"
        private const val SQL_QUERY_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        private val SQL_QUERY_NEXT_UNSENT_PAYLOAD =
            "SELECT * FROM $TABLE_NAME ORDER BY ${COL_PRIMARY_KEY.name} ASC LIMIT 1"

        private fun createPayloadDeleteQuery(nonce: String) =
            "DELETE FROM $TABLE_NAME WHERE $COL_NONCE = \"${nonce}\""
    }
}

private data class Column(val index: Int, val name: String) {
    override fun toString() = name
}

private fun ContentValues.put(column: Column, value: String) = put(column.name, value)

private fun Cursor.getString(column: Column): String = getString(column.index)
