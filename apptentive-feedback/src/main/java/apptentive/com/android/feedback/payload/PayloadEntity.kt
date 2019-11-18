package apptentive.com.android.feedback.payload

data class PayloadEntity(
    val nonce: String,
    val type: String,
    val mediaType: String
) {
    companion object {
        fun fromModel(payload: Payload): PayloadEntity {
            return PayloadEntity(
                nonce = payload.nonce,
                type = payload.type.toString(),
                mediaType = payload.mediaType.toString()
            )
        }
    }
}