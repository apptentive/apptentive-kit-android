package apptentive.com.android.feedback.model

import java.io.File

enum class ConversationState {
    /** Conversation state is not known */
    UNDEFINED,
    /** No logged in user and no conversation token */
    ANONYMOUS_PENDING,
    /** No logged in user with conversation token */
    ANONYMOUS,
    /** The activeConversation belongs to the currently logged-in user */
    LOGGED_IN,
    /** The activeConversation belongs to a logged-out user */
    LOGGED_OUT
}

data class Conversation(
    val localIdentifier: String,
    val dataFile: File,
    val messagesFile: File,
    val state: ConversationState = ConversationState.UNDEFINED
)