package apptentive.com.android.feedback.redux

import apptentive.com.android.feedback.model.AppState
import apptentive.com.android.feedback.model.Conversation
import org.rekotlin.DispatchFunction
import org.rekotlin.Middleware

internal val conversationSaveMiddleWare: Middleware<AppState> = { dispatch, getState ->
    { next ->
        { action ->
            // first, we have to update the state
            next(action)

            // then, we can save it
            if (action is ConversationMutatingAction) {
                val conversation = getState()!!.activeConversation
                try {
                    saveConversation(conversation, dispatch)
                } catch (e: Exception) {
                    dispatch(ConversationSaveFailedAction(conversation.localIdentifier, e))
                }
            }
        }
    }
}

private fun saveConversation(
    conversation: Conversation,
    dispatch: DispatchFunction
) {
}

