package apptentive.com.android.feedback.payload

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PayloadSQLiteDatabaseHandler(
    context: Context,
    factory: SQLiteDatabase.CursorFactory
) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        factory,
        DATABASE_VERSION
    ) {

    companion object {
        private const val DATABASE_NAME = "PayloadsDB"
        private const val DATABASE_VERSION = 1
        val TABLE_NAME = "Payloads"
        val COL_ID = "uuid"
        val COL_PAYLOAD_TYPE = "payloadType"
        val COL_MEDIA_TYPE = "mediaType"
        val COL_DATA = "data"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = ("CREATE TABLE " +
                TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY," +
                COL_PAYLOAD_TYPE + " TEXT," +
                COL_MEDIA_TYPE + " TEXT," +
                COL_DATA + " BLOB" + ")")
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addPayload(payload: Payload) {
        val values = ContentValues()
        values.put(COL_ID, payload.nonce)
        values.put(COL_PAYLOAD_TYPE, Payload.PayloadType)
        values.put(COL_MEDIA_TYPE, payload.mediaType.toString())
        values.put(COL_DATA, payload.data)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun deletePayload(payload: Payload): Boolean {
        //todo arg sho
        var result = false
        val query =
            "SELECT * FROM $TABLE_NAME WHERE $COL_ID = \"${payload.nonce}\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            db.delete(TABLE_NAME, COL_ID + " = ?",
                arrayOf(id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result

    }

    fun fetchPayload(payload: Payload){
//todo arg will have id or payload object?
    }
}