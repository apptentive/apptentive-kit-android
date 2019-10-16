package apptentive.com.android.feedback.conversation

import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.ConversationTokenFetchResponse
import apptentive.com.android.feedback.mockAppRelease
import apptentive.com.android.feedback.mockDevice
import apptentive.com.android.feedback.mockPerson
import apptentive.com.android.feedback.mockSdk
import apptentive.com.android.feedback.model.*
import apptentive.com.android.feedback.test.TestCase
import apptentive.com.android.util.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

class ConversationManagerTest : TestCase() {
    @Test
    fun getActiveConversation() {
        val fetchResponse = ConversationTokenFetchResponse(
            id = "id",
            deviceId = "device_id",
            personId = "person_id",
            token = "token",
            encryptionKey = "encryption_key"
        )

        val conversationManager = ConversationManager(
            conversationRepository = MockConversationRepository,
            conversationService = MockConversationService(fetchResponse)
        )

        val conversation: Conversation = conversationManager.activeConversation.value
        assertThat(conversation.conversationToken).isEqualTo(fetchResponse.token)
        assertThat(conversation.conversationId).isEqualTo(fetchResponse.id)
        assertThat(conversation.person.id).isEqualTo(fetchResponse.personId)
    }

    @Test
    @Ignore
    fun corruptedConversationData() {
    }

    @Test
    @Ignore
    fun conversationDataMigration() {
    }
}

private object MockConversationRepository : ConversationRepository {
    private var conversation: Conversation? = null

    override fun createConversation(): Conversation {
        return Conversation(
            localIdentifier = "localIdentifier",
            device = mockDevice,
            person = mockPerson,
            sdk = mockSdk,
            appRelease = mockAppRelease,
            engagementManifest = EngagementManifest(),
            engagementData = EngagementData()
        )
    }

    override fun saveConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    override fun loadConversation(): Conversation? = conversation
}

private class MockConversationService(
    private val response: ConversationTokenFetchResponse
) :
    ConversationService {
    override fun fetchConversationToken(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        callback: (Result<ConversationTokenFetchResponse>) -> Unit
    ) {
        callback(Result.Success(response))
    }

    override fun fetchEngagementManifest(
        conversationToken: String,
        conversationId: String,
        callback: (Result<EngagementManifest>) -> Unit
    ) {
        callback(Result.Success(EngagementManifest(expiry = getTimeSeconds() + 1800)))
    }

}


