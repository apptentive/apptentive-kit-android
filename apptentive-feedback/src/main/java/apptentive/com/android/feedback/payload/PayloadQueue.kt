package apptentive.com.android.feedback.payload

interface PayloadQueue {
    fun enqueuePayload(payload: Payload)
    fun nextUnsentPayload() : Payload?
    fun deletePayload(payload: Payload)
}