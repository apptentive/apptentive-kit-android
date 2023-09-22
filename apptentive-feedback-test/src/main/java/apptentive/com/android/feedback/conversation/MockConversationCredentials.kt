package apptentive.com.android.feedback.conversation

import apptentive.com.android.encryption.EncryptionKey

class MockConversationCredentials : ConversationCredentialProvider {
    override val conversationToken: String = "mockedConversationToken"
    override val conversationId: String = "mockedConversationId"
    override val payloadEncryptionKey: EncryptionKey? = null // Provide a mock encryption key
    override val conversationPath: String = "mockedConversationPath"
}
