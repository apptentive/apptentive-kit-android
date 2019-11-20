package apptentive.com.android.feedback.payload

interface PayloadSender {
    fun sendPayload(payload: Payload)
}