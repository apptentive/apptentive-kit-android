package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.randomUUID
import org.rekotlin.StateType

// FIXME: figure out a better name since this one might be confused with the host app state
data class AppState(
    val activeConversation: Conversation
) : StateType {

    companion object {
        internal fun initialState(): AppState = AppState(Conversation(randomUUID()))
    }
}