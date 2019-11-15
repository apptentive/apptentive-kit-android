package apptentive.com.android.feedback.payload

interface PayloadSender {
    fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit)
}