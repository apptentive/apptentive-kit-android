package apptentive.com.android.feedback.redux

import apptentive.com.android.convert.Serializer
import apptentive.com.android.debug.Assert.assertEqual
import apptentive.com.android.feedback.backend.ConversationFetchService
import apptentive.com.android.feedback.model.ApptentiveState
import apptentive.com.android.feedback.model.Conversation
import org.rekotlin.DispatchFunction

internal interface Middleware<T> {
    fun apply(dispatch: DispatchFunction, getState: () -> T?): (DispatchFunction) -> DispatchFunction
}

internal class ConversationSaveMiddleware(private val conversationSerializer: Serializer) : Middleware<ApptentiveState> {
    override fun apply(dispatch: DispatchFunction, getState: () -> ApptentiveState?): (DispatchFunction) -> DispatchFunction =
        { next ->
            { action ->
                // first, we have to update the state
                next(action)

                // then, we can save it
                if (action is ConversationMutatingAction) {
                    val conversation = getState()!!.activeConversation

                    // check if we still target the correct conversation
                    assertEqual(action.localConversationIdentifier, conversation.localIdentifier)

                    try {
                        saveConversation(conversation)
                    } catch (e: Exception) {
                        dispatch(ConversationSaveFailedAction(conversation.localIdentifier, e))
                    }
                }
            }
        }

    private fun saveConversation(conversation: Conversation) {
        conversationSerializer.serialize(conversation)
    }
}

internal class ConversationFetchMiddleware(private val service: ConversationFetchService) : Middleware<ApptentiveState> {
    override fun apply(dispatch: DispatchFunction, getState: () -> ApptentiveState?): (DispatchFunction) -> DispatchFunction =
        { next ->
            { action ->
                // first, we have to update the state
                next(action)

                if (action is ConversationFetchAction) {
                    val conversation = getState()!!.activeConversation

                    // check if we still target the correct conversation
                    assertEqual(action.localConversationIdentifier, conversation.localIdentifier)

                    // fetch conversations
                    service.fetchConversation()
                        .then { credentials ->
                            dispatch(
                                ConversationFetchCompletedAction(
                                    action.localConversationIdentifier,
                                    credentials.identifier,
                                    credentials.token
                                )
                            )
                        }
                        .catch { error ->
                            dispatch(
                                ConversationFetchFailedAction(
                                    action.localConversationIdentifier,
                                    error
                                )
                            )
                        }
                }
            }
        }
}