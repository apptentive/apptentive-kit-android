package apptentive.com.android.feedback.payload

class SDKPayload(
    nonce: String,
    val authorEmail: String,
    val authorName: String,
    val distribution: String,
    val distributionVersion: String,
    val platform: String,
    val programmingLanguage: String,
    val version: String
) : Payload(nonce) {
}

