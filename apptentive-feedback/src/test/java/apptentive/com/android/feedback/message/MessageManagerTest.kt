package apptentive.com.android.feedback.conversation

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.backend.MessageFetchService
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.feedback.payload.MockPayloadSender
import apptentive.com.android.util.Result
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
        messageManager = MessageManager(
            "1234",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository()
        )

        val engagementContextFactory = object : EngagementContextFactory {
            override fun engagementContext(): EngagementContext {
                return engagementContext
            }
        }

        DependencyProvider.register(engagementContextFactory as EngagementContextFactory)
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
        // No messages in the storage
        var messageManager = MessageManager(
            "1234",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository(listOf())
        )
        messageManager.onAppForeground()
        Assert.assertFalse(messageManager.pollingScheduler.isPolling())

        // At least one message
        messageManager = MessageManager(
            "1234",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository()
        )
        messageManager.onAppForeground()
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
            type = "Text",
            sender = Sender("123", "Tester", null),
            body = "ABC Hidden",
            hidden = true
        )

        messageManager.sendMessage("ABC Hidden", true)

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
            type = "Text",
            sender = Sender("123", "Tester", null),
            body = "ABC",
            hidden = false
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
        messageManager.setCustomData(CustomData())
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

private class MockMessageFetchService : MessageFetchService {
    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        callback(Result.Success(MessageList(testMessageList, null, null)))
    }
}

private class MockExecutor : Executor {
    override fun execute(task: () -> Unit) {
        task()
    }
}

private class MockMessageRepository(val messageList: List<Message> = testMessageList) : MessageRepository {
    override fun addOrUpdateMessage(messages: List<Message>) {}

    override fun getLastReceivedMessageIDFromEntries(): String = ""

    override fun getAllMessages(): List<Message> {
        return messageList
    }

    override fun saveMessages() {}

    override fun deleteMessage(nonce: String) {}

    override fun updateMessage(message: Message) {}
}
