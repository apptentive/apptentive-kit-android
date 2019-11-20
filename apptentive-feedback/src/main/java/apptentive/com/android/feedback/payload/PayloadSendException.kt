package apptentive.com.android.feedback.payload

open class PayloadSendException(
    val payload: Payload,
    detailMessage: String? = null,
    cause: Throwable? = null
) :
    Exception(createMessage(payload, detailMessage), cause) {
    companion object {
        fun createMessage(payload: Payload, detailMessage: String?): String {
            return if (detailMessage != null) {
                "Payload sending failed $payload: $detailMessage"
            } else {
                "Payload sending failed $payload"
            }
        }
    }
}
