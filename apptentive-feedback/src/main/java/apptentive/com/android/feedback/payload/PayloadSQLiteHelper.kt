package apptentive.com.android.feedback.payload

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PayloadSQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " +
                TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY," +
                COL_PAYLOAD_TYPE + " TEXT," +
                COL_MEDIA_TYPE + " TEXT," +
                COL_DATA + " BLOB" + ")")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addPayload(payload: Payload) {
        val values = ContentValues().apply {
            put(COL_ID, payload.nonce)
            put(COL_PAYLOAD_TYPE, payload.type.toString())
            put(COL_MEDIA_TYPE, payload.mediaType.toString())
            put(COL_DATA, payload.data)
        }

        writableDatabase.use { db ->
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun deletePayload(payload: Payload): Boolean {
        val query = "DELETE * FROM $TABLE_NAME WHERE $COL_ID = \"${payload.nonce}\" LIMIT 1"
        writableDatabase.use { db ->
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    return true
                }
            }
        }
        return false

    }

    fun getNextUnsentPayload(): Payload? {
        TODO()
    }

    companion object {
        private const val DATABASE_NAME = "payload_db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "payloads"
        const val COL_ID = "uuid"
        const val COL_PAYLOAD_TYPE = "payload_type"
        const val COL_MEDIA_TYPE = "media_type"
        const val COL_DATA = "data"
    }
}