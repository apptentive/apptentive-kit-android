package apptentive.com.android.feedback.conversation

import apptentive.com.android.core.encryption.EncryptionKey

internal interface ConversationCredentialProvider {
    val conversationToken: String?
    val conversationId: String?
    val payloadEncryptionKey: EncryptionKey?
    val conversationPath: String
}

internal data class ConversationCredential(
    override val conversationToken: String? = null,
    override val conversationId: String? = null,
    override val payloadEncryptionKey: EncryptionKey? = null,
    override val conversationPath: String
) : ConversationCredentialProvider
