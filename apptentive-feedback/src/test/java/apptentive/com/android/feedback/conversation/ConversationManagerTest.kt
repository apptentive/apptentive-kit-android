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
import apptentive.com.android.util.Factory
import apptentive.com.android.util.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ConversationManagerTest : TestCase() {
    @Test
    fun getActiveConversation() {
        val fetchResponse = ConversationTokenFetchResponse(
            id = "id",
            device_id = "device_id",
            person_id = "person_id",
            token = "token",
            encryption_key = "encryption_key"
        )

        val conversationSerializer: ConversationSerializer = MockConversationSerializer()
        val appReleaseFactory = MockAppReleaseFactory(mockAppRelease)
        val personFactory = MockPersonFactory(mockPerson)
        val deviceFactory = MockDeviceFactory(mockDevice)
        val sdkFactory = MockSdkFactory(mockSdk)
        val conversationManager = ConversationManager(
            conversationSerializer = conversationSerializer,
            appReleaseFactory = appReleaseFactory,
            personFactory = personFactory,
            deviceFactory = deviceFactory,
            sdkFactory = sdkFactory,
            conversationService = MockConversationService(fetchResponse)
        )
        val conversation: Conversation = conversationManager.activeConversation.value
        assertThat(conversation.conversationToken).isEqualTo(fetchResponse.token)
        assertThat(conversation.conversationId).isEqualTo(fetchResponse.id)
        assertThat(conversation.person.id).isEqualTo(fetchResponse.person_id)
    }
}

private class MockConversationSerializer :
    ConversationSerializer {
    private var conversation: Conversation? = null

    override fun saveConversation(conversation: Conversation) {
        this.conversation = conversation.copy()
    }

    override fun loadConversation(): Conversation? = conversation
}

data class MockAppReleaseFactory(private val appRelease: AppRelease) : Factory<AppRelease> {
    override fun create() = appRelease
}

private class MockPersonFactory(private val person: Person) : Factory<Person> {
    override fun create() = person
}

private class MockDeviceFactory(private val device: Device) : Factory<Device> {
    override fun create() = device
}

private class MockSdkFactory(private val sdk: SDK) : Factory<SDK> {
    override fun create() = sdk
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


