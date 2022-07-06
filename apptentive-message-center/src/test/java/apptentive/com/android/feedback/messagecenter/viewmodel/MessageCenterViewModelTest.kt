package apptentive.com.android.feedback.messagecenter.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Provider
import apptentive.com.android.core.isInThePast
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.backend.MessageFetchService
import apptentive.com.android.feedback.dependencyprovider.MessageCenterModelProvider
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.message.MessageManagerFactoryProvider
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents.EVENT_NAME_CLOSE
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.utils.convertToGroupDate
import apptentive.com.android.util.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

val testMessageList: List<Message> = listOf(
    Message(
        id = "Test",
        nonce = "UUID",
        type = "MC",
        body = "Hello",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis())
    ),
    Message(
        id = "Test2",
        nonce = "UUID2",
        type = "MC2",
        body = "Hello2",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - DAY_IN_MILLIS)
    ),
    Message(
        id = "Test3",
        nonce = "UUID3",
        type = "MC3",
        body = "Hello3",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS + 100))
    ),
    Message(
        id = "Test4",
        nonce = "UUID4",
        type = "MC4",
        body = "Hello4",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 7))
    ),
    Message(
        id = "Test5",
        nonce = "UUID5",
        type = "MC5",
        body = "Hello5",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 365))
    ),
    Message(
        id = "Test6 Hidden",
        nonce = "UUID6",
        type = "MC6",
        body = "Hello6",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - 1000),
        hidden = true
    )
)

class MessageCenterViewModelTest : TestCase() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        val messageManager = MessageManager(
            "conversationId",
            "token",
            MockMessageFetchService(),
            MockExecutor(),
            MockMessageRepository()
        )
        DependencyProvider.register(
            MessageCenterModelProvider(
                MessageCenterInteraction(
                    messageCenterId = "12345",
                    title = "Message center",
                    branding = "Branding",
                    composer = MessageCenterInteraction.Composer(
                        title = "Composer title",
                        hintText = "",
                        sendButton = null,
                        sendStart = null,
                        sendOk = null,
                        sendFail = null,
                        closeText = "",
                        closeBody = "",
                        closeDiscard = "",
                        closeCancel = ""
                    ),
                    greeting = MessageCenterInteraction.Greeting("", "", ""),
                    status = MessageCenterInteraction.Status(""),
                    automatedMessage = MessageCenterInteraction.AutomatedMessage(""),
                    errorMessage = null,
                    profile = null
                )
            )
        )
        DependencyProvider.register(MessageManagerFactoryProvider(messageManager))
        DependencyProvider.register(
            MockEngagementContextFactory
            {
                MockEngagementContext(
                    onEngage = { args ->
                        addResult(args)
                        EngagementResult.InteractionNotShown("No runnable interactions")
                    },
                    onSendPayload = { payload ->
                        throw AssertionError("We didn't expect any payloads here but this one slipped though: $payload")
                    }
                )
            }
        )
    }

    @Test
    fun testNewMessages() {
        val viewModel = MessageCenterViewModel()
        val manager = DependencyProvider.of<MessageManagerFactory>().messageManager()
        manager.fetchMessages()
        addResult(viewModel.messages)
        assertResults(testMessageList.filterNot { it.hidden })
    }

    @Test
    fun testExitMessageCenter() {
        val viewModel = MessageCenterViewModel()
        viewModel.exitMessageCenter()
        assertResults(
            createCall(EVENT_NAME_CLOSE, "12345")
        )
    }

    private fun createCall(codePoint: String, interactionId: String) =
        EngageArgs(
            event = Event.internal(codePoint, interaction = InteractionType.MessageCenter),
            interactionId = interactionId
        )

    @Test
    fun testGetAndGroupMessages() {
        val viewModel = MessageCenterViewModel()
        val now = convertToGroupDate(toSeconds(System.currentTimeMillis())) // DayOfWeek MM/DD
        val dayAgo = convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 1))) // DayOfWeek MM/DD
        val weekAgo = convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 7))) // MM/DD
        val yearAgo = convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 365))) // MM/DD/YYYY

        viewModel.messages.forEach { assertTrue(isInThePast(it.createdAt)) }
        assertEquals(now, viewModel.messages[0].groupTimestamp)
        assertEquals(dayAgo, viewModel.messages[1].groupTimestamp)
        assertNull(viewModel.messages[2].groupTimestamp) // If same day, don't show group timestamp
        assertEquals(weekAgo, viewModel.messages[3].groupTimestamp)
        assertEquals(yearAgo, viewModel.messages[4].groupTimestamp)
    }
}

class MockMessageFetchService : MessageFetchService {
    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        callback(Result.Success(MessageList(testMessageList, null, null)))
    }
}

class MockExecutor : Executor {
    override fun execute(task: () -> Unit) {
        task()
    }
}

class MockEngagementContextFactory(val getEngagementContext: () -> EngagementContext) :
    Provider<EngagementContextFactory> {
    override fun get(): EngagementContextFactory {
        return object : EngagementContextFactory {
            override fun engagementContext(): EngagementContext {
                return getEngagementContext()
            }
        }
    }
}

class MockMessageRepository : MessageRepository {
    override fun getLastReceivedMessageIDFromEntries(): String = ""

    override fun addOrUpdateMessage(messages: List<Message>) {}

    override fun getAllMessages(): List<Message> {
        return testMessageList
    }

    override fun saveMessages() {}

    override fun deleteMessage(nonce: String) {}

    override fun updateMessage(message: Message) {}
}
