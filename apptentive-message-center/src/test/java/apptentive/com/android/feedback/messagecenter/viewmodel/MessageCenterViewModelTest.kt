package apptentive.com.android.feedback.messagecenter.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Provider
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
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents.EVENT_NAME_CLOSE
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.util.Result
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

val testMessageList: List<Message> = listOf(
    Message(
        id = "Test",
        nonce = "UUID",
        type = "MC",
        body = "Hello",
        sender = null,
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
            TestMessageFetchService(),
            TestExecutor()
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
        assertResults(testMessageList)
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
}

class TestMessageFetchService : MessageFetchService {
    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        callback(Result.Success(MessageList(testMessageList, null, null)))
    }
}

class TestExecutor : Executor {
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
