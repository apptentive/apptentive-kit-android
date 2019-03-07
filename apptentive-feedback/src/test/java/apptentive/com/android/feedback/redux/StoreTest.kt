package apptentive.com.android.feedback.redux

import apptentive.com.android.convert.Deserializer
import apptentive.com.android.convert.Serializer
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Conversation.State.*
import org.junit.Test

import org.junit.Assert.*

class StoreTest {
    @Test
    fun testConversationActions() {
        val storage = MemoryStorage<Conversation>()

        val middleware = listOf(ConversationSaveMiddleware(storage))
        val store = createStore(middleware)

        /* 1. Initial state should contain an 'undefined' conversation */
        assertEquals(UNDEFINED, store.state.activeConversation.state)

        val localConversationId = store.state.activeConversation.localIdentifier

        /* 2. Conversation becomes 'pending' while fetching conversation token and identifier */
        store.dispatch(ConversationFetchAction(localConversationId))
        assertEquals(PENDING, store.state.activeConversation.state)

        assertEquals(storage.deserialize(), store.state.activeConversation)

        /* 3. Conversation becomes 'pending' while fetching conversation token and identifier */
        store.dispatch(ConversationFetchCompletedAction(localConversationId, "id", "token"))
        assertEquals(ANONYMOUS, store.state.activeConversation.state)
        assertEquals("id", store.state.activeConversation.identifier)
        assertEquals("token", store.state.activeConversation.token)

        assertEquals(storage.deserialize(), store.state.activeConversation)
    }
}

private class MemoryStorage<T: Any> : Serializer, Deserializer {
    private var target: T? = null

    override fun serialize(obj: Any) {
        @Suppress("UNCHECKED_CAST")
        target = obj as T
    }

    override fun deserialize(): Any {
        return target ?: throw IllegalStateException("Storage is empty")
    }
}