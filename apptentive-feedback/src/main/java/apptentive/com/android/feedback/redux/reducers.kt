package apptentive.com.android.feedback.redux

import apptentive.com.android.debug.Assert.assertEqual
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.ApptentiveState
import org.rekotlin.Action

internal fun rootReducer(action: Action, state: ApptentiveState?): ApptentiveState {
    val state = state ?: ApptentiveState.initialState()
    return state.copy(
        activeConversation = conversationReducer(action, state.activeConversation)
    )
}

private fun conversationReducer(
    action: Action,
    conversation: Conversation
): Conversation {
    if (action is ConversationAction) {
        return conversationReducer(action, conversation)
    }

    return conversation
}

private fun conversationReducer(
    action: ConversationAction,
    conversation: Conversation
): Conversation {
    if (conversation.localIdentifier != action.localConversationIdentifier) {
        // FIXME: error message
        return conversation
    }

    if (action is ConversationFetchAction) {
        assertEqual(
            conversation.state,
            Conversation.State.UNDEFINED,
            "Unexpected conversation state: ${conversation.state}"
        )
        return conversation.copy(state = Conversation.State.PENDING)
    }

    if (action is ConversationFetchCompletedAction) {
        assertEqual(
            conversation.state,
            Conversation.State.PENDING,
            "Unexpected conversation state: ${conversation.state}"
        )
        return conversation.copy(
            state = Conversation.State.ANONYMOUS,
            identifier = action.conversationIdentifier,
            token = action.conversationToken
        )
    }

    if (action is ConversationFetchFailedAction) {
        assertEqual(
            conversation.state,
            Conversation.State.PENDING,
            "Unexpected conversation state: ${conversation.state}"
        )
        // FIXME: error message
        return conversation.copy(
            state = Conversation.State.CORRUPTED
        )
    }

    return conversation
}
