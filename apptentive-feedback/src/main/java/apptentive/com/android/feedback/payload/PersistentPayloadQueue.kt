package apptentive.com.android.feedback.payload

import android.content.Context

class PersistentPayloadQueue(
    private val dbHelper: PayloadSQLiteHelper
) : PayloadQueue {
    override fun enqueuePayload(payload: PayloadData) {
        dbHelper.addPayload(payload)
    }

    override fun nextUnsentPayload(): PayloadData? {
        return dbHelper.nextUnsentPayload()
    }

    override fun deletePayload(payload: PayloadData) {
        dbHelper.deletePayload(payload.nonce)
    }

    companion object {
        fun create(context: Context) = PersistentPayloadQueue(
            dbHelper = PayloadSQLiteHelper(context)
        )
    }
}