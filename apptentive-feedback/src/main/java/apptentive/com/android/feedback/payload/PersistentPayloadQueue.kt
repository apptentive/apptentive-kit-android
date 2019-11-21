package apptentive.com.android.feedback.payload

class PersistentPayloadQueue(
    private val dbHelper: PayloadSQLiteHelper
) : PayloadQueue {
    override fun enqueuePayload(payload: Payload) {
        dbHelper.addPayload(payload)
    }

    override fun nextUnsentPayload(): Payload? {
        return dbHelper.nextUnsentPayload()
    }

    override fun deletePayload(payload: Payload) {
        dbHelper.deletePayload(payload.nonce)
    }
}