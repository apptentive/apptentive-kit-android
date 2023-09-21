package apptentive.com.android.feedback.payload

import apptentive.com.android.encryption.Encryption

data class PayloadContext(
    val tag: String,
    val conversationId: String?,
    val token: String?,
    val encryption: Encryption,
    val sessionId: String?
)
