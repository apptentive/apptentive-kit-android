package apptentive.com.android.feedback.conversation

import apptentive.com.android.encryption.EncryptionKey

interface ConversationCredentialProvider

data class ConversationCredentials(
    val conversationToken: String? = null,
    val conversationId: String? = null,
    val payloadEncryptionKey: EncryptionKey? = null,
    val conversationPath: String? = null
) : ConversationCredentialProvider