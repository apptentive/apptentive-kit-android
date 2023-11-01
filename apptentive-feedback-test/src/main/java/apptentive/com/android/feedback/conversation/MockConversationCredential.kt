package apptentive.com.android.feedback.conversation

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.encryption.KeyResolver23
import apptentive.com.android.encryption.getKeyFromHexString

class MockConversationCredential : ConversationCredentialProvider {
    override val conversationToken: String = "mockedConversationToken"
    override val conversationId: String = "mockedConversationId"
    override val payloadEncryptionKey: EncryptionKey? = null // Provide a mock encryption key
    override val conversationPath: String = "mockedConversationPath"
}

class MockEncryptedConversationCredential : ConversationCredentialProvider {
    override val conversationToken: String = "mockedConversationToken"
    override val conversationId: String = "mockedConversationId"
    @RequiresApi(Build.VERSION_CODES.M)
    override val payloadEncryptionKey: EncryptionKey? = EncryptionKey("73F22C02E59D47FD8D1CD4CFD1B7C87A73F22C02E59D47FD8D1CD4CFD1B7C87A".getKeyFromHexString(), "AES/CBC/PKCS5Padding")
    override val conversationPath: String = "mockedConversationPath"
}

