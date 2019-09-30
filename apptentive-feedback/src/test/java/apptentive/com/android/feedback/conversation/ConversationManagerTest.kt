package apptentive.com.android.feedback.conversation

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
import org.junit.Test

class ConversationManagerTest : TestCase() {
    @Test
    fun getActiveConversation() {
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
            conversationService = MockConversationService()
        )
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

private class MockConversationService : ConversationService {
    override fun fetchConversationToken(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        callback: (Result<ConversationTokenFetchResponse>) -> Unit
    ) {
    }

    override fun fetchEngagementManifest(
        conversationToken: String,
        conversationId: String,
        callback: (Result<EngagementManifest>) -> Unit
    ) {
        TODO()
    }

}


