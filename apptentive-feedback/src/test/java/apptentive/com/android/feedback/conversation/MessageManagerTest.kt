package apptentive.com.android.feedback.conversation

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.feedback.backend.MessageFetchService
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.util.Result
import org.junit.Assert
import org.junit.Test

class MessageManagerTest : TestCase() {

    @Test
    fun testStartPolling() {
        val messageManager = MessageManager(
            "1234",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository()
        )
        messageManager.onConversationChanged(testConversation)
        messageManager.onAppForeground()
        Assert.assertTrue(messageManager.pollingScheduler.isPolling())
    }

    @Test
    fun testStopPolling() {
        val messageManager = MessageManager(
            "1234",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository()
        )
        messageManager.onConversationChanged(testConversation)
        messageManager.onAppForeground()
        Assert.assertTrue(messageManager.pollingScheduler.isPolling())
        messageManager.onAppBackground()
        Assert.assertTrue(!messageManager.pollingScheduler.isPolling())
    }

    @Test
    fun testNewMessages() {
        val messageManager = MessageManager(
            "1234",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository()
        )
        messageManager.fetchMessages()
        addResult(messageManager.messages.value)
        assertResults(testMessageList)
    }
}

val testConversation: Conversation = Conversation(
    "",
    device = Device("", "", "", 1, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 1),
    person = Person("", "", "", ""),
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

private class MockMessageRepository : MessageRepository {
    override fun addOrUpdateMessage(message: List<Message>) {}

    override fun getLastReceivedMessageIDFromEntries(): String = ""

    override fun getAllMessages(): List<Message> {
        return testMessageList
    }

    override fun saveMessages() {}

    override fun deleteMessage(nonce: String) {}

    override fun updateMessage(message: Message) {}
}
