package apptentive.com.android.feedback.conversation

import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.encryption.getKeyFromHexString

class MockConversationCredential : ConversationCredentialProvider {
    override val conversationToken: String = "mockedConversationToken"
    override val conversationId: String = "mockedConversationId"
    override val payloadEncryptionKey: EncryptionKey? = null // Provide a mock encryption key
    override val conversationPath: String = "mockedConversationPath"
}

class MockEncryptedConversationCredential : ConversationCredentialProvider {
    override val conversationToken: String = "mockedEncryptedConversationToken"
    override val conversationId: String = "mockedEncryptedConversationId"
    override val payloadEncryptionKey: EncryptionKey? = EncryptionKey("73F22C02E59D47FD8D1CD4CFD1B7C87A73F22C02E59D47FD8D1CD4CFD1B7C87A".getKeyFromHexString(), "AES/CBC/PKCS5Padding")
    override val conversationPath: String = "mockedEncryptedConversationPath"
}

class MockUpdatedConversationCredential : ConversationCredentialProvider {
    override val conversationToken: String = "mockedUpdatedConversationToken"
    override val conversationId: String = "mockedConversationId"
    override val payloadEncryptionKey: EncryptionKey? = null // Provide a mock encryption key
    override val conversationPath: String = "mockedConversationPath"
}

class MockUpdatedEncryptedConversationCredential : ConversationCredentialProvider {
    override val conversationToken: String = "mockedUpdatedConversationToken"
    override val conversationId: String = "mockedEncryptedConversationId"
    override val payloadEncryptionKey: EncryptionKey? = EncryptionKey("73F22C02E59D47FD8D1CD4CFD1B7C87A73F22C02E59D47FD8D1CD4CFD1B7C87A".getKeyFromHexString(), "AES/CBC/PKCS5Padding")
    override val conversationPath: String = "mockedEncryptedConversationPath"
}
