package apptentive.com.android.feedback.message

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.backend.MessageCenterService
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.conversation.MockConversationCredential
import apptentive.com.android.feedback.conversation.MockEncryptedConversationCredential
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.feedback.model.payloads.MultipartParser
import apptentive.com.android.feedback.payload.MockPayloadSender
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class MessageManagerTest : TestCase() {

    private lateinit var messageManager: MessageManager
    private val engagementContext = MockEngagementContext()
    private val payloadSender = engagementContext.getPayloadSender() as MockPayloadSender

    @Before
    fun setup() {
        val engagementContextFactory = object : EngagementContextFactory {
            override fun engagementContext(): EngagementContext {
                return engagementContext
            }
        }
        DependencyProvider.register(engagementContextFactory as EngagementContextFactory)
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())

        messageManager = MessageManager(
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository(),
        )
        messageManager.onConversationChanged(testConversation)
    }

    @Test
    fun testStartPolling() {
        messageManager.onAppForeground()
        Assert.assertTrue(messageManager.pollingScheduler.isPolling())
    }

    @Test
    fun testStopPolling() {
        messageManager.onAppForeground()
        Assert.assertTrue(messageManager.pollingScheduler.isPolling())
        messageManager.onAppBackground()
        Assert.assertTrue(!messageManager.pollingScheduler.isPolling())
    }

    @Test
    fun testHasSentMessage() {
        // App launched with at least one message in the storage
        var messageManager = MessageManager(
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository(),
        )
        messageManager.onAppForeground()
        Assert.assertTrue(messageManager.pollingScheduler.isPolling())

        // App is launched with no messages in the storage
        messageManager = MessageManager(
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository(listOf()),
        )
        messageManager.onAppForeground()
        Assert.assertFalse(messageManager.pollingScheduler.isPolling())

        // Send a message, should trigger polling
        messageManager.onConversationChanged(testConversation)
        messageManager.sendMessage("Sending a message")

        Assert.assertTrue(messageManager.pollingScheduler.isPolling())
    }

    @Test
    fun testNewMessages() {
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())
        messageManager.fetchMessages()
        addResult(messageManager.messages.value)
        assertResults(testMessageList)
    }

    @Test
    fun testSendHiddenMessage() {
        val expectedPayload = MessagePayload(
            body = "ABC Hidden",
            hidden = true,
            messageNonce = generateUUID(),
            automated = null,
            attachments = emptyList()
        )

        messageManager.sendMessage("ABC Hidden", isHidden = true)

        val actualPayload = payloadSender.payload as MessagePayload?
        assertEquals(expectedPayload.body, actualPayload?.body)
        assertEquals(expectedPayload.hidden, actualPayload?.hidden)
    }

    @Test
    fun testSendMessage() {
        val payloadSender = engagementContext.getPayloadSender() as MockPayloadSender
        val expectedPayload = MessagePayload(
            body = "ABC",
            hidden = null,
            messageNonce = generateUUID(),
            automated = null,
            attachments = emptyList()
        )

        messageManager.sendMessage("ABC")

        val actualPayload = payloadSender.payload as MessagePayload?
        assertEquals(expectedPayload.body, actualPayload?.body)
        assertEquals(expectedPayload.hidden, actualPayload?.hidden)

        val credentialProvider = DependencyProvider.of<ConversationCredentialProvider>()
        val actualPayloadData = actualPayload?.toPayloadData(credentialProvider)

        val inputStream = ByteArrayInputStream(actualPayloadData?.data)
        val parser = MultipartParser(inputStream, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")

        assertEquals(1, parser.numberOfParts)

        val firstPart = parser.getPartAtIndex(0)!!

        assertEquals("Content-Disposition: form-data; name=\"message\"\r\n" +
                        "Content-Type: application/json;charset=UTF-8", firstPart.headers)

        val json = JsonConverter.toMap(String(firstPart.content, Charsets.UTF_8))
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", json["session_id"])
        assertTrue(1698774495.52 < json["client_created_at"] as Double)
        assertEquals("ABC", json["body"])
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", json["nonce"])
        assertEquals(-25200.0, json["client_created_at_utc_offset"])
        assertEquals(null, json["token"])
    }

    @Test
    fun testSendMessageEncrypted() {
        val conversationCredential = MockEncryptedConversationCredential()
        DependencyProvider.register<ConversationCredentialProvider>(conversationCredential)

        val payloadSender = engagementContext.getPayloadSender() as MockPayloadSender
        val expectedPayload = MessagePayload(
            body = "ABC",
            hidden = null,
            messageNonce = generateUUID(),
            automated = null,
            attachments = emptyList()
        )

        messageManager.sendMessage("ABC")

        val actualPayload = payloadSender.payload as MessagePayload?
        assertEquals(expectedPayload.body, actualPayload?.body)
        assertEquals(expectedPayload.hidden, actualPayload?.hidden)

        val credentialProvider = DependencyProvider.of<ConversationCredentialProvider>()
        val actualPayloadData = actualPayload?.toPayloadData(credentialProvider)

        val inputStream = ByteArrayInputStream(actualPayloadData?.data)
        val parser = MultipartParser(inputStream, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")

        assertEquals(1, parser.numberOfParts)

        val firstPart = parser.getPartAtIndex(0)!!

        assertEquals("Content-Disposition: form-data; name=\"message\"\r\n" +
                "Content-Type: application/octet-stream", firstPart.headers)

        val decryptedContent = AESEncryption23(conversationCredential.payloadEncryptionKey!!).decryptPayloadData(firstPart.content)
        val decryptedPart = MultipartParser.parsePart(ByteArrayInputStream(decryptedContent), 0L..decryptedContent.size + 2) // TODO: Why do we have to add 2 here?

        assertEquals("Content-Disposition: form-data; name=\"message\"\r\n" +
                "Content-Type: application/json;charset=UTF-8", decryptedPart!!.headers)

        val json = JsonConverter.toMap(String(decryptedPart!!.content, Charsets.UTF_8))
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", json["session_id"])
        assertTrue(1698774495.52 < json["client_created_at"] as Double)
        assertEquals("ABC", json["body"])
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", json["nonce"])
        assertEquals(-25200.0, json["client_created_at_utc_offset"])
        assertEquals("mockedConversationToken", json["token"])
    }

    // TODO: Test encrypted and unencrypted with attachments

    @Test
    fun testCustomDataCleanup() {
        messageManager.setCustomData(emptyMap())
        messageManager.sendMessage("Hello")
        assertTrue(messageManager.messageCustomData == null)
    }
}

val testConversation: Conversation = Conversation(
    "",
    device = Device("", "", "", 1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 1),
    person = Person("123", "test@test.com", "Tester", ""),
    sdk = SDK("", ""),
    appRelease = AppRelease("", "", 1L, "", "", "")
)

val testMessageList: List<Message> = listOf(
    Message(
        id = "Test",
        nonce = "UUID",
        type = "MC",
        body = "Hello",
        sender = null,
        read = null
    ),
    Message(
        id = "Test2",
        nonce = "UUID2",
        type = "MC2",
        body = "Hello2",
        sender = null,
        hidden = true
    )
)

internal class MockMessageCenterService : MessageCenterService {
    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        callback(Result.Success(MessageList(testMessageList, null, null)))
    }

    override fun getAttachment(remoteUrl: String, callback: (Result<ByteArray>) -> Unit) {
        TODO("Not yet implemented")
    }
}

internal class MockExecutor : Executor {
    override fun execute(task: () -> Unit) {
        task()
    }
}

internal class MockMessageRepository(private var messageList: List<Message> = testMessageList) : MessageRepository {
    override fun addOrUpdateMessages(messages: List<Message>) {
        messageList = messages
    }

    override fun getLastReceivedMessageIDFromEntries(): String = ""

    override fun getAllMessages(): List<Message> {
        return messageList
    }

    override fun saveMessages() {}

    override fun deleteMessage(nonce: String) {}

    override fun updateEncryption(encryption: Encryption) {}
    override fun updateConversationRoster(conversationRoster: ConversationRoster) {}
    override fun logout() {}
}
