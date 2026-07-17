package apptentive.com.android.feedback

import apptentive.com.android.TestCase
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Provider
import apptentive.com.android.core.concurrent.Executor
import apptentive.com.android.core.concurrent.Executors
import apptentive.com.android.core.encryption.Encrypted
import apptentive.com.android.core.encryption.NotEncrypted
import apptentive.com.android.core.network.HttpClient
import apptentive.com.android.core.network.HttpRequest
import apptentive.com.android.core.network.HttpResponse
import apptentive.com.android.core.network.Result
import apptentive.com.android.core.platform.AndroidSharedPrefDataStore
import apptentive.com.android.core.platform.SharedPrefConstants.PENDING_DEVICE_CUSTOMDATA_UPDATE
import apptentive.com.android.core.platform.SharedPrefConstants.PENDING_PERSON_CUSTOMDATA_UPDATE
import apptentive.com.android.core.platform.SharedPrefConstants.SDK_CORE_INFO
import apptentive.com.android.feedback.backend.ConversationFetchResponse
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.MockConversationCredential
import apptentive.com.android.feedback.conversation.MockConversationRepository
import apptentive.com.android.feedback.conversation.createConversationManager
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.feedback.engagement.util.MockFileSystem
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MockExecutor
import apptentive.com.android.feedback.message.MockMessageCenterService
import apptentive.com.android.feedback.message.MockMessageRepository
import apptentive.com.android.feedback.message.testMessageList
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageCenterNotification
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.payloads.DevicePayload
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.model.payloads.PersonPayload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.feedback.platform.SDKEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

internal class MockEngagementContextFactory(val getEngagementContext: () -> EngagementContext) :
    Provider<EngagementContextFactory> {
    override fun get(): EngagementContextFactory {
        return object : EngagementContextFactory {
            override fun engagementContext(): EngagementContext {
                return getEngagementContext()
            }
        }
    }
}

class ApptentiveDefaultClientTest : TestCase() {
    private var messageManager = MessageManager(
        MockMessageCenterService(),
        MockExecutor(),
        MockMessageRepository(),
    )

    private val mockHttpClient = object : HttpClient {
        override fun <T : Any> send(
            request: HttpRequest<T>,
            callback: (Result<HttpResponse<T>>) -> Unit
        ) {}
    }

    private val mockExecutor = object : Executor {
        override fun execute(task: () -> Unit) {
            task()
        }
    }

    private val sentPayloads = mutableListOf<Payload>()

    private val mockPayloadSender = object : PayloadSender {
        override fun enqueuePayload(payload: Payload, credentialProvider: ConversationCredentialProvider) {
            sentPayloads.add(payload)
        }
        override fun updateCredential(credentialProvider: ConversationCredentialProvider) {}
    }

    private val sharedPrefDataStore = MockAndroidSharedPrefDataStore()

    @Before
    fun setup() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(sharedPrefDataStore)
        DependencyProvider.register<FileSystem>(MockFileSystem())
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())
        Apptentive.messageCenterNotificationSubject.value = null
    }

    @After
    fun clean() {
        DependencyProvider.clear()
    }

    @Ignore("Failing on Jenkins. Unknown reason.")
    @Test
    fun testUpdateAndGetPersonName() {
        val testName = "Test Name"
        val testName2 = "Name Test 2"

        val apptentiveClient = getApptentiveClient()

        assertNull(apptentiveClient.getPersonName())

        apptentiveClient.updatePerson(testName)

        assertEquals(testName, apptentiveClient.getPersonName())

        apptentiveClient.updatePerson(testName2)

        assertEquals(testName2, apptentiveClient.getPersonName())
    }

    @Ignore("Failing on Jenkins. Unknown reason.")
    @Test
    fun testUpdateAndGetPersonEmail() {
        val testEmail = "test@email.com"
        val testEmail2 = "email@test.com"

        val apptentiveClient = getApptentiveClient()

        assertNull(apptentiveClient.getPersonEmail())

        apptentiveClient.updatePerson(email = testEmail)

        assertEquals(testEmail, apptentiveClient.getPersonEmail())

        apptentiveClient.updatePerson(email = testEmail2)

        assertEquals(testEmail2, apptentiveClient.getPersonEmail())
    }

    @Test
    fun testMigrationFrom600() {
        DefaultStateMachine.reset()
        DependencyProvider.clear()
        // Migrating from 6.0.0, has storage but CRYPTO_ENABLED flag
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore(containsKey = false))
        DependencyProvider.register<FileSystem>(MockFileSystem())
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))

        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)

        val apptentiveClient = getApptentiveClient()
        val encryptionStatus = apptentiveClient.getOldEncryptionSetting()
        assertEquals(NotEncrypted, encryptionStatus)
    }

    @Test
    fun testNotEncryptedStatus() {
        DefaultStateMachine.reset()
        // Not encrypted, has storage & contains CRYPTO_ENABLED flag
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore(containsKey = true))
        DependencyProvider.register<FileSystem>(MockFileSystem(containsFile = true))

        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)

        val apptentiveClient = getApptentiveClient()
        val encryptionStatus = apptentiveClient.getOldEncryptionSetting()
        assertEquals(NotEncrypted, encryptionStatus)
    }

    @Test
    fun testEncryptedStatus() {
        DefaultStateMachine.reset()
        // Encrypted, has storage & CRYPTO_ENABLED flag true
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore(containsKey = true, isEncryptionEnabled = true))
        DependencyProvider.register<FileSystem>(MockFileSystem(containsFile = true))

        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        val apptentiveClient = getApptentiveClient()

        val encryptionStatus = apptentiveClient.getOldEncryptionSetting()
        assertEquals(Encrypted, encryptionStatus)
    }

    @Ignore("Failing on Jenkins. Unknown reason.")
    @Test
    fun testUpdateMessageCenterNotification() {
        var expected = MessageCenterNotification(false, 1, null, null)

        var count = 0
        val notification = Apptentive.messageCenterNotificationSubject.observe {
            // Default state should be null
            if (count == 0) assertEquals(null, it)
            else assertEquals(expected, it)
            count++
        }

        // Update Person
        expected = expected.copy(personName = "Test name", personEmail = "Test email")
        mockPerson = Person(name = "Test name", email = "Test email")
        getApptentiveClient().updateMessageCenterNotification()

        // Update messages
        getApptentiveClient().messageManager?.updateMessages(
            testMessageList + Message(
                id = "Test3",
                nonce = "UUID3",
                type = "MC3",
                body = "Hello3",
                sender = null,
                read = null
            )
        )

        expected = expected.copy(unreadMessageCount = 2)
        getApptentiveClient().updateMessageCenterNotification()
        notification.unsubscribe()
    }

    @Ignore("Failing on Jenkins. Unknown reason.")
    @Test
    fun testDontUpdateMessageCenterNotification() {
        var count = 0
        messageManager.updateMessages(testMessageList)
        val notification = Apptentive.messageCenterNotificationSubject.observe {
            when (count) {
                // Default state should be null
                0 -> assertEquals(null, it)
                1 -> assertEquals(
                    MessageCenterNotification(
                        canShowMessageCenter = false,
                        unreadMessageCount = 1,
                        personName = "Test Name 1",
                        personEmail = "Test Email 1"
                    ),
                    it
                )
                2 -> assertEquals(
                    MessageCenterNotification(
                        canShowMessageCenter = false,
                        unreadMessageCount = 1,
                        personName = "Test Name 2",
                        personEmail = "Test Email 1"
                    ),
                    it
                )
                3 -> assertEquals(
                    MessageCenterNotification(
                        canShowMessageCenter = false,
                        unreadMessageCount = 1,
                        personName = "Test Name 2",
                        personEmail = "Test Email 2"
                    ),
                    it
                )
                4 -> assertEquals(
                    MessageCenterNotification(
                        canShowMessageCenter = false,
                        unreadMessageCount = 1,
                        personName = "Test Name 3",
                        personEmail = "Test Email 3"
                    ),
                    it
                )
                else -> assertTrue(false)
            }
            count++
        }

        // Update Person
        mockPerson = Person(name = "Test Name 1", email = "Test Email 1")
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        mockPerson = Person(name = "Test Name 2", email = "Test Email 1")
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        mockPerson = Person(name = "Test Name 2", email = "Test Email 2")
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        mockPerson = Person(name = "Test Name 3", email = "Test Email 3")
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        getApptentiveClient().updateMessageCenterNotification()
        assertEquals(count, 5)
        notification.unsubscribe()
    }

    //region Custom data update batching

    // Drives the state machine through RegisterSDK -> ClientStarted so the conversation roster is
    // initialized (getApptentiveClient only fires ClientStarted), then returns a ready client.
    private fun startedClient(): ApptentiveDefaultClient {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        return getApptentiveClient()
    }

    private fun isDevicePending() =
        sharedPrefDataStore.getBoolean(SDK_CORE_INFO, PENDING_DEVICE_CUSTOMDATA_UPDATE, false)

    private fun isPersonPending() =
        sharedPrefDataStore.getBoolean(SDK_CORE_INFO, PENDING_PERSON_CUSTOMDATA_UPDATE, false)

    @Test
    fun testUpdateDeviceCustomDataDefersPayload() {
        val client = startedClient()

        // Device custom data updates are always batched, never sent immediately.
        client.updateDevice("new_key" to "new_value")

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isDevicePending())
    }

    @Test
    fun testUpdateDeviceWithExistingKeyDefersPayload() {
        val client = startedClient()

        // "key" already exists in mockDevice with value "value" — updating it is also deferred.
        client.updateDevice("key" to "updated_value")

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isDevicePending())
    }

    @Test
    fun testUpdateDeviceDeleteKeyDefersPayload() {
        val client = startedClient()

        client.updateDevice(deleteKey = "key")

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isDevicePending())
    }

    @Test
    fun testUpdateDeviceWithSameValueDoesNothing() {
        val client = startedClient()

        // No actual change — device is unchanged, so nothing is enqueued or flagged.
        client.updateDevice("key" to "value")

        assertTrue(sentPayloads.isEmpty())
        assertFalse(isDevicePending())
    }

    @Test
    fun testFlushPendingCustomDataUpdateEnqueuesAndClears() {
        val client = startedClient()

        client.updateDevice("key" to "updated_value")
        assertTrue(sentPayloads.isEmpty())
        assertTrue(isDevicePending())

        client.flushPendingCustomDataUpdate()

        assertEquals(1, sentPayloads.count { it is DevicePayload })
        assertFalse(isDevicePending())
    }

    @Test
    fun testFlushWithNoPendingUpdateEnqueuesNothing() {
        val client = startedClient()

        client.flushPendingCustomDataUpdate()

        assertTrue(sentPayloads.isEmpty())
    }

    @Test
    fun testFlushWithoutConversationCredentialRetainsFlag() {
        val client = startedClient()

        client.updateDevice("key" to "updated_value")
        assertTrue(isDevicePending())

        // Simulate a cold start where the conversation credential isn't available yet: the flush
        // must not send (it would be dropped) and must keep the flag so it retries on next launch.
        DependencyProvider.clear()

        client.flushPendingCustomDataUpdate()

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isDevicePending())
    }

    @Test
    fun testUpdateMParticleIdClearsPendingPersonCustomData() {
        val client = startedClient()

        // Defer a person custom data change, then set the mParticle id.
        client.updatePerson(customData = "person_key" to "updated_value")
        assertTrue(isPersonPending())

        client.updateMParticleID("new_mparticle_id")

        // The person payload carries the custom data snapshot, so the flag is cleared.
        assertEquals(1, sentPayloads.count { it is PersonPayload })
        assertFalse(isPersonPending())
    }

    @Test
    fun testSetPushIntegrationClearsPendingDeviceCustomData() {
        // setPushIntegration mutates the (shared) mockDevice.integrationConfig in place, so restore
        // it afterwards to avoid polluting other tests that assert on the pristine mockDevice.
        val originalApptentiveIntegration = mockDevice.integrationConfig.apptentive
        try {
            val client = startedClient()

            // Defer a device custom data change, then set a push integration.
            client.updateDevice("key" to "updated_value")
            assertTrue(isDevicePending())

            client.setPushIntegration(Apptentive.PUSH_PROVIDER_APPTENTIVE, "push_token")

            // The device payload carries the custom data snapshot, so the flag is cleared.
            assertEquals(1, sentPayloads.count { it is DevicePayload })
            assertFalse(isDevicePending())
        } finally {
            mockDevice.integrationConfig.apptentive = originalApptentiveIntegration
        }
    }

    @Test
    fun testUpdatePersonCustomDataDefersPayload() {
        val client = startedClient()

        // Person custom data updates are always batched, never sent immediately.
        client.updatePerson(customData = "new_person_key" to "new_value")

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isPersonPending())
    }

    @Test
    fun testUpdatePersonWithExistingCustomDataKeyDefersPayload() {
        val client = startedClient()

        // "person_key" already exists in mockPerson with value "person_value".
        client.updatePerson(customData = "person_key" to "updated_value")

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isPersonPending())
    }

    @Test
    fun testUpdatePersonDeleteKeyDefersPayload() {
        val client = startedClient()

        client.updatePerson(deleteKey = "person_key")

        assertTrue(sentPayloads.isEmpty())
        assertTrue(isPersonPending())
    }

    @Test
    fun testFlushPendingPersonUpdateEnqueuesAndClears() {
        val client = startedClient()

        client.updatePerson(customData = "person_key" to "updated_value")
        assertTrue(sentPayloads.isEmpty())
        assertTrue(isPersonPending())

        client.flushPendingCustomDataUpdate()

        assertEquals(1, sentPayloads.count { it is PersonPayload })
        assertFalse(isPersonPending())
    }

    @Test
    fun testFlushSendsBothDeferredDeviceAndPersonUpdates() {
        val client = startedClient()

        client.updateDevice("key" to "updated_value")
        client.updatePerson(customData = "person_key" to "updated_value")
        assertTrue(sentPayloads.isEmpty())

        client.flushPendingCustomDataUpdate()

        assertEquals(1, sentPayloads.count { it is DevicePayload })
        assertEquals(1, sentPayloads.count { it is PersonPayload })
        assertFalse(isDevicePending())
        assertFalse(isPersonPending())
    }

    @Test
    fun testUpdatePersonNameEnqueuesPayloadImmediately() {
        val client = startedClient()

        client.updatePerson(name = "Test Name")

        val payload = sentPayloads.filterIsInstance<PersonPayload>().single()
        assertEquals("Test Name", payload.name)
        // Name is not custom data, so it is enqueued immediately and never sets the deferral flag.
        assertFalse(isPersonPending())
    }

    @Test
    fun testUpdatePersonEmailEnqueuesPayloadImmediately() {
        val client = startedClient()

        client.updatePerson(email = "test@email.com")

        val payload = sentPayloads.filterIsInstance<PersonPayload>().single()
        assertEquals("test@email.com", payload.email)
        // Email is not custom data, so it is enqueued immediately and never sets the deferral flag.
        assertFalse(isPersonPending())
    }

    @Test
    fun testUpdatePersonNameEnqueuedImmediatelyAndFlushesPendingPersonCustomData() {
        val client = startedClient()

        // Defer a person custom data change first — nothing sent, flag set.
        client.updatePerson(customData = "person_key" to "updated_value")
        assertTrue(sentPayloads.isEmpty())
        assertTrue(isPersonPending())

        // A name update is enqueued immediately, and its payload carries the deferred custom data...
        client.updatePerson(name = "New Name")

        val payload = sentPayloads.filterIsInstance<PersonPayload>().single()
        assertEquals("New Name", payload.name)
        assertEquals("updated_value", payload.customData?.get("person_key"))
        // ...so the pending person flag is cleared to avoid a redundant send on the next flush.
        assertFalse(isPersonPending())
    }

    @Test
    fun testUpdatePersonEmailEnqueuedImmediatelyAndFlushesPendingPersonCustomData() {
        val client = startedClient()

        client.updatePerson(customData = "person_key" to "updated_value")
        assertTrue(sentPayloads.isEmpty())
        assertTrue(isPersonPending())

        client.updatePerson(email = "test@email.com")

        val payload = sentPayloads.filterIsInstance<PersonPayload>().single()
        assertEquals("test@email.com", payload.email)
        assertEquals("updated_value", payload.customData?.get("person_key"))
        assertFalse(isPersonPending())
    }

    @Test
    fun testUpdatePersonNameDoesNotClearPendingDeviceCustomData() {
        val client = startedClient()

        // Defer a device custom data change — a person payload does not carry device custom data,
        // so a person name/email update must not clear the device deferral (else it would be lost).
        client.updateDevice("key" to "updated_value")
        assertTrue(isDevicePending())

        client.updatePerson(name = "New Name")

        assertEquals(1, sentPayloads.count { it is PersonPayload })
        assertEquals(0, sentPayloads.count { it is DevicePayload })
        assertTrue(isDevicePending())
    }

    //endregion

    private fun getApptentiveClient(): ApptentiveDefaultClient {
        val apptentiveClient = ApptentiveDefaultClient(
            configuration = ApptentiveConfiguration("KEY", "SIGNATURE"),
            httpClient = mockHttpClient,
            executors = Executors(
                state = mockExecutor,
                main = mockExecutor
            )
        )

        val fetchResponse = ConversationFetchResponse(
            id = "id",
            deviceId = "device_id",
            personId = "person_id",
            token = "token",
            encryptionKey = "encryption_key"
        )
        DependencyProvider.register<ConversationRepository>(MockConversationRepository())
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)

        apptentiveClient.conversationManager = createConversationManager(fetchResponse)
        apptentiveClient.payloadSender = mockPayloadSender
        apptentiveClient.messageManager = messageManager

        return apptentiveClient
    }
}
