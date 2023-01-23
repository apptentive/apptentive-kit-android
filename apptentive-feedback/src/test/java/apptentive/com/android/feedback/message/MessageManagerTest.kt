package apptentive.com.android.feedback.message

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.backend.MessageCenterService
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
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.feedback.payload.MockPayloadSender
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
        messageManager = MessageManager(
            "1234",
            "token",
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository()
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
            "1234",
            "token",
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository()
        )
        messageManager.onAppForeground()
        Assert.assertTrue(messageManager.pollingScheduler.isPolling())

        // App is launched with no messages in the storage
        messageManager = MessageManager(
            "1234",
            "token",
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository(listOf())
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
        messageManager.fetchMessages()
        addResult(messageManager.messages.value)
        assertResults(testMessageList)
    }

    @Test
    fun testSendHiddenMessage() {
        val expectedPayload = MessagePayload(
            type = Message.MESSAGE_TYPE_TEXT,
            sender = Sender("123", "Tester", null),
            body = "ABC Hidden",
            hidden = true,
            boundary = generateUUID(),
            messageNonce = generateUUID(),
            automated = null,
            attachments = emptyList()
        )

        messageManager.sendMessage("ABC Hidden", isHidden = true)

        val actualPayload = payloadSender.payload as MessagePayload?
        assertEquals(expectedPayload.sender, actualPayload?.sender)
        assertEquals(expectedPayload.type, actualPayload?.type)
        assertEquals(expectedPayload.body, actualPayload?.body)
        assertEquals(expectedPayload.hidden, actualPayload?.hidden)
    }

    @Test
    fun testSendMessage() {
        val payloadSender = engagementContext.getPayloadSender() as MockPayloadSender
        val expectedPayload = MessagePayload(
            type = Message.MESSAGE_TYPE_TEXT,
            sender = Sender("123", "Tester", null),
            body = "ABC",
            hidden = null,
            boundary = generateUUID(),
            messageNonce = generateUUID(),
            automated = null,
            attachments = emptyList()
        )

        messageManager.sendMessage("ABC")

        val actualPayload = payloadSender.payload as MessagePayload?
        assertEquals(expectedPayload.sender, actualPayload?.sender)
        assertEquals(expectedPayload.type, actualPayload?.type)
        assertEquals(expectedPayload.body, actualPayload?.body)
        assertEquals(expectedPayload.hidden, actualPayload?.hidden)
    }

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
}
