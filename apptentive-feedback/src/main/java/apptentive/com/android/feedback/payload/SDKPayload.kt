package apptentive.com.android.feedback.payload

class SDKPayload(
    nonce: String,
    val author_email: String,
    val author_name: String,
    val distribution: String,
    val distribution_version: String,
    val platform: String,
    val programming_language: String,
    val version: String
) : Payload(nonce) {
}

