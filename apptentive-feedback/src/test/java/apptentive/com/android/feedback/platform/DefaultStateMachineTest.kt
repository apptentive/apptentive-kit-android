package apptentive.com.android.feedback.platform

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.MockAndroidLoggerProvider
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.MockConversationRepository
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.message.MockMessageRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DefaultStateMachineTest : TestCase() {

    @Before
    override fun setUp() {
        super.setUp()
        DependencyProvider.register(MockAndroidLoggerProvider())
        DependencyProvider.register<ConversationRepository>(MockConversationRepository())
        DependencyProvider.register<MessageRepository>(MockMessageRepository())
        DefaultStateMachine.reset()
    }

    @After
    fun tearDown() {
        DefaultStateMachine.reset()
    }

    @Test
    fun testInitialState() {
        val stateMachine = DefaultStateMachine
        assertEquals(stateMachine.state, SDKState.UNINITIALIZED)
    }

    @Test
    fun testRegister() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        assertEquals(DefaultStateMachine.state, SDKState.LOADING_APPTENTIVE_CLIENT_DEPENDENCIES)
    }

    @Test
    fun testClientStarted() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        assertEquals(DefaultStateMachine.state, SDKState.LOADING_CONVERSATION_MANAGER_DEPENDENCIES)
    }

    @Test
    fun testInvalidTransition() {
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        assertEquals(DefaultStateMachine.state, SDKState.UNINITIALIZED)
    }

    @Test
    fun testLoadingConversation() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        assertEquals(DefaultStateMachine.state, SDKState.LOADING_CONVERSATION)
    }

    @Test
    fun testConversationLoaded() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.ConversationAnonymous)
        assertEquals(DefaultStateMachine.state, SDKState.ANONYMOUS)
    }

    @Test
    fun testPendingToken() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        assertEquals(DefaultStateMachine.state, SDKState.PENDING_TOKEN)
    }

    @Test
    fun testLogin() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        DefaultStateMachine.onEvent(SDKEvent.ConversationAnonymous)
        assertEquals(DefaultStateMachine.state, SDKState.ANONYMOUS)
        DefaultStateMachine.onEvent(SDKEvent.LoggedIn("test", EncryptionKey(), ByteArray(0)))
        assertEquals(DefaultStateMachine.state, SDKState.LOGGED_IN)
    }

    @Test
    fun testLogout() {
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        DefaultStateMachine.onEvent(SDKEvent.ConversationAnonymous)
        assertEquals(DefaultStateMachine.state, SDKState.ANONYMOUS)
        DefaultStateMachine.onEvent(SDKEvent.LoggedIn("test", EncryptionKey(), ByteArray(0)))
        assertEquals(DefaultStateMachine.state, SDKState.LOGGED_IN)
        DefaultStateMachine.onEvent(SDKEvent.Logout("Id"))
        assertEquals(DefaultStateMachine.state, SDKState.LOGGED_OUT)
    }
}
