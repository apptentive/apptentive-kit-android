package apptentive.com.android.feedback.redux

import apptentive.com.android.feedback.model.Conversation
import org.junit.Test

import org.junit.Assert.*

class StoreTest {
    @Test
    fun testConversationActions() {
        val store = createStore()

        // initial state should contain an undefined conversation
        assertEquals(Conversation.State.UNDEFINED, store.state.activeConversation.state)

        val localConversationId = store.state.activeConversation.localIdentifier

        // conversation becomes 'pending' while fetching conversation token and identifier
        store.dispatch(ConversationFetchAction(localConversationId))
        assertEquals(Conversation.State.PENDING, store.state.activeConversation.state)

        // conversation becomes 'pending' while fetching conversation token and identifier
        store.dispatch(ConversationFetchCompletedAction(localConversationId, "id", "token"))
        assertEquals(Conversation.State.ANONYMOUS, store.state.activeConversation.state)
        assertEquals("id", store.state.activeConversation.identifier)
        assertEquals("token", store.state.activeConversation.token)
    }
}