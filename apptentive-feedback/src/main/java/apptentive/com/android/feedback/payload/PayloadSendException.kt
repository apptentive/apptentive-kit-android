package apptentive.com.android.feedback.payload

internal open class PayloadSendException(
    val payload: PayloadData,
    detailMessage: String? = null,
    cause: Throwable? = null
) :
    Exception(createMessage(payload, detailMessage), cause) {
    companion object {
        private fun createMessage(payload: PayloadData, detailMessage: String?): String {
            return if (detailMessage != null) {
                "Payload sending failed $payload: $detailMessage"
            } else {
                "Payload sending failed $payload"
            }
        }
    }
}
