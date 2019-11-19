package apptentive.com.android.feedback.payload

data class PayloadMetadata(
    val nonce: String,
    val type: String,
    val mediaType: String
) {
    companion object {
        fun fromModel(payload: Payload): PayloadMetadata {
            return PayloadMetadata(
                nonce = payload.nonce,
                type = payload.type.toString(),
                mediaType = payload.mediaType.toString()
            )
        }
    }
}