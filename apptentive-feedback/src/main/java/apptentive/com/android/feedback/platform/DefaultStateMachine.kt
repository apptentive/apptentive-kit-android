package apptentive.com.android.feedback.platform

import apptentive.com.android.encryption.Encryption
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.conversation.ConversationState
import apptentive.com.android.feedback.utils.RosterUtils.initializeRoster
import apptentive.com.android.feedback.utils.RosterUtils.updateRosterForLogin
import apptentive.com.android.feedback.utils.RosterUtils.updateRosterForLogout
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.STATE_MACHINE

internal object DefaultStateMachine : StateMachine(SDKState.UNINITIALIZED) {
    lateinit var conversationRoster: ConversationRoster
    lateinit var encryption: Encryption

    val readyState = listOf(SDKState.READY, SDKState.ANONYMOUS, SDKState.LOGGED_IN)
    val loadingState = listOf(SDKState.UNINITIALIZED, SDKState.LOADING_APPTENTIVE_CLIENT_DEPENDENCIES, SDKState.LOADING_CONVERSATION_MANAGER_DEPENDENCIES, SDKState.LOADING_CONVERSATION, SDKState.PENDING_TOKEN)
    val errorState = listOf(SDKState.ERROR)
    var conversationCredentials: ConversationCredentials? = null

    init {
        onState(SDKState.UNINITIALIZED) {
            initState {
                Log.d(STATE_MACHINE, "UNINITIALIZED")
            }
            transition(SDKEvent.RegisterSDK.name, SDKState.LOADING_APPTENTIVE_CLIENT_DEPENDENCIES)
        }
        onState(SDKState.LOADING_APPTENTIVE_CLIENT_DEPENDENCIES) {
            initState {
                Log.d(STATE_MACHINE, "LOADING_APPTENTIVE_CLIENT_DEPENDENCIES")
            }
            transition(SDKEvent.ClientStarted.name, SDKState.LOADING_CONVERSATION_MANAGER_DEPENDENCIES)
        }
        onState(SDKState.LOADING_CONVERSATION_MANAGER_DEPENDENCIES) {
            initState {
                Log.d(STATE_MACHINE, "LOADING_CONVERSATION_MANAGER_DEPENDENCIES")
                conversationRoster = initializeRoster()
            }
            transition(SDKEvent.LoadingConversation.name, SDKState.LOADING_CONVERSATION)
            transition(SDKEvent.Logout.name, SDKState.LOGGED_OUT)
            transition(SDKEvent.Error.name, SDKState.ERROR)
        }
        onState(SDKState.LOADING_CONVERSATION) {
            initState {
                Log.d(STATE_MACHINE, "LOADING_CONVERSATION")
            }
            transition(SDKEvent.ConversationLoaded.name, SDKState.ANONYMOUS)
            transition(SDKEvent.LoggedIn.name, SDKState.LOGGED_IN)
            transition(SDKEvent.PendingToken.name, SDKState.PENDING_TOKEN)
            transition(SDKEvent.Error.name, SDKState.ERROR)
        }
        onState(SDKState.PENDING_TOKEN) {
            initState {
                conversationRoster.activeConversation =
                    conversationRoster.activeConversation?.copy(state = ConversationState.AnonymousPending)
                Log.d(STATE_MACHINE, "PENDING_TOKEN")
            }
            transition(SDKEvent.ConversationLoaded.name, SDKState.ANONYMOUS)
            transition(SDKEvent.Error.name, SDKState.ERROR)
        }
        onState(SDKState.ANONYMOUS) {
            initState {
                conversationRoster.activeConversation =
                    conversationRoster.activeConversation?.copy(state = ConversationState.Anonymous)
                if (it is SDKEvent.ConversationLoaded) {
                    conversationCredentials = ConversationCredentials(
                        conversationId = it.conversationId,
                        conversationToken = it.conversationToken
                    )
                }
                Log.d(STATE_MACHINE, "ANONYMOUS")
            }
            transition(SDKEvent.LoggedIn.name, SDKState.LOGGED_IN)
            transition(SDKEvent.Error.name, SDKState.ERROR)
        }
        onState(SDKState.LOGGED_IN) {
            initState {
                Log.d(STATE_MACHINE, "LOGGED_IN")
                if (it is SDKEvent.LoggedIn) {
                    updateRosterForLogin(it.subject, it.encryption)
//                    conversationCredentials = ConversationCredentials(
//                        conversationId = it.conversationId,
//                        conversationToken = it.conversationToken
//                    )
                }
            }
            transition(SDKEvent.Logout.name, SDKState.LOGGED_OUT)
            transition(SDKEvent.Error.name, SDKState.ERROR)
        }
        onState(SDKState.LOGGED_OUT) {
            initState {
                Log.d(STATE_MACHINE, "LOGGED_OUT")
                if (it is SDKEvent.Logout) {
                    updateRosterForLogout(it.conversationId)
                }
            }
            transition(SDKEvent.LoggedIn.name, SDKState.LOGGED_IN)
            transition(SDKEvent.Error.name, SDKState.ERROR)
        }
        onState(SDKState.ERROR) {
            initState {
                Log.d(STATE_MACHINE, "ERROR")
            }
        }
    }
}

@InternalUseOnly
enum class SDKState {
    // SDK is uninitialized. register is not called
    UNINITIALIZED,
    // register is called, loads existing roster
    LOADING_APPTENTIVE_CLIENT_DEPENDENCIES,
    // loading roster
    LOADING_CONVERSATION_MANAGER_DEPENDENCIES,
    // Loading conversation file or creates new ones
    LOADING_CONVERSATION,
    // new conversation is created. Conversation token fetch request is sent and waiting for the response
    PENDING_TOKEN,
    // conversation token is received. Conversation is ready to be used
    READY,
    // conversation is anonymous
    ANONYMOUS,
    // conversation is authenticated by a JWT token
    LOGGED_IN,
    // No active anonymous or logged in conversation. Logged out conversation(s) is/are available
    LOGGED_OUT,
    // SDK has hit an error state like invalid JWT token, conversation deserialization error etc.
    // TODO split into recoverable and unrecoverable error states

    // TODO Recoverable manifest expiry without internet,
    //  jwt token expiry - can customer recover the state by logging in again with a valid jwt?

    ERROR
}

internal sealed class SDKEvent {
    // register process started
    object RegisterSDK : SDKEvent()
    // starting apptentive client
    object ClientStarted : SDKEvent()
    // conversation loading process started
    object LoadingConversation : SDKEvent()
    // conversation token fetch request is sent
    object PendingToken : SDKEvent()
    // conversation is loaded
    data class ConversationLoaded(
        val conversationId: String,
        val conversationToken: String
    ) : SDKEvent() {
        companion object {
            const val name = "ConversationLoaded"
        }
    }
    // log out completed
    data class Logout(val conversationId: String) : SDKEvent() {
        companion object {
            const val name = "Logout"
        }
    }
    // logged in completed
    data class LoggedIn(
        val subject: String,
        val encryption: EncryptionKey
    ) : SDKEvent() {
        companion object {
            const val name = "LoggedIn"
        }
    }
    // an error occurred
    object Error : SDKEvent()

    val name = this::class.simpleName ?: ""
}

data class ConversationCredentials(val conversationId: String, val conversationToken: String)

internal fun DefaultStateMachine.isSDKReady() = readyState.contains(state)
internal fun DefaultStateMachine.isSDKLoading() = loadingState.contains(state)
