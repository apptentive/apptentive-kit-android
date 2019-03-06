package apptentive.com.android.feedback.model

import org.rekotlin.StateType

data class Conversation(
    val localIdentifier: String,
    val identifier: String = "",
    val token: String = "",
    val state: State = State.UNDEFINED
) : StateType {
    enum class State {
        /** Conversation state is not known */
        UNDEFINED,
        /** No logged in user and no conversation token */
        PENDING,
        /** No logged in user with conversation token */
        ANONYMOUS,
        /** The activeConversation belongs to the currently logged-in user */
        LOGGED_IN,
        /** The activeConversation belongs to a logged-out user */
        LOGGED_OUT,
        /** Conversation failed to load */
        CORRUPTED;
    }
}