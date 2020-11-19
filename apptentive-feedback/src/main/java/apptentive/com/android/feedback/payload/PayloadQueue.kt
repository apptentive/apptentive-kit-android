package apptentive.com.android.feedback.payload

interface PayloadQueue {
    fun enqueuePayload(payload: PayloadData)
    fun nextUnsentPayload() : PayloadData?
    fun deletePayload(payload: PayloadData)
}