package apptentive.com.android.feedback.platform

import apptentive.com.android.TestCase
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.MockConversationRepository
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.message.MockMessageRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class DefaultStateMachineTest : TestCase() {

    @Before
    override fun setUp() {
        super.setUp()
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        DependencyProvider.register<ConversationRepository>(MockConversationRepository())
        DependencyProvider.register<MessageRepository>(MockMessageRepository())
    }

    @After
    fun tearDown() {
        DefaultStateMachine.reset()
    }

    @Test
    fun testInitialState() {
        DefaultStateMachine.reset()
        val stateMachine = DefaultStateMachine
        assertEquals(stateMachine.state, SDKState.UNINITIALIZED)
    }

    @Ignore
    @Test
    fun testRegister() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        assertEquals(DefaultStateMachine.state, SDKState.LOADING_APPTENTIVE_CLIENT_DEPENDENCIES)
    }

    @Ignore
    @Test
    fun testClientStarted() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        assertEquals(DefaultStateMachine.state, SDKState.LOADING_CONVERSATION_MANAGER_DEPENDENCIES)
    }

    @Test
    fun testInvalidTransition() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        assertEquals(DefaultStateMachine.state, SDKState.UNINITIALIZED)
    }

    @Ignore
    @Test
    fun testLoadingConversation() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        assertEquals(DefaultStateMachine.state, SDKState.LOADING_CONVERSATION)
    }

    @Ignore
    @Test
    fun testConversationLoaded() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.ConversationLoaded)
        assertEquals(DefaultStateMachine.state, SDKState.READY)
    }

    @Ignore
    @Test
    fun testPendingToken() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        assertEquals(DefaultStateMachine.state, SDKState.PENDING_TOKEN)
    }

    @Ignore
    @Test
    fun testLogin() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        DefaultStateMachine.onEvent(SDKEvent.ConversationLoaded)
        assertEquals(DefaultStateMachine.state, SDKState.ANONYMOUS)
        DefaultStateMachine.onEvent(SDKEvent.LoggedIn("test", EncryptionKey()))
        assertEquals(DefaultStateMachine.state, SDKState.LOGGED_IN)
    }

    @Ignore
    @Test
    fun testLogout() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        DefaultStateMachine.onEvent(SDKEvent.ConversationLoaded)
        assertEquals(DefaultStateMachine.state, SDKState.ANONYMOUS)
        DefaultStateMachine.onEvent(SDKEvent.LoggedIn("test", EncryptionKey()))
        assertEquals(DefaultStateMachine.state, SDKState.LOGGED_IN)
        DefaultStateMachine.onEvent(SDKEvent.Logout("Id"))
        assertEquals(DefaultStateMachine.state, SDKState.LOGGED_OUT)
    }
}
