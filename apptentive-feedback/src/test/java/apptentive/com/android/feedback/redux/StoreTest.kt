package apptentive.com.android.feedback.redux

import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.convert.Deserializer
import apptentive.com.android.convert.Serializer
import apptentive.com.android.feedback.backend.ConversationCredentials
import apptentive.com.android.feedback.backend.ConversationFetchService
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Conversation.State.*
import apptentive.com.android.feedback.test.TestCase
import org.junit.Test

import org.junit.Assert.*

class StoreTest: TestCase() {
    private lateinit var executor: ImmediateExecutorQueue

    override fun setUp() {
        super.setUp()
        executor = ImmediateExecutorQueue("dispatch", dispatchManually = true)
    }

    @Test
    fun testConversationActions() {
        // serialize everything to memory
        val storage = MemoryStorage<Conversation>()

        val credentials = ConversationCredentials("identifier", "token")
        val fetchService = MockConversationFetchService(credentials, executor)

        val middleware = listOf(
            ConversationFetchMiddleware(fetchService),
            ConversationSaveMiddleware(storage)
        )
        val store = createStore(middleware)

        /* 1. Initial state should contain an 'undefined' conversation */
        assertEquals(UNDEFINED, store.state.activeConversation.state)

        val localConversationId = store.state.activeConversation.localIdentifier

        /* 2. Conversation becomes 'pending' while fetching conversation token and identifier */
        store.dispatch(ConversationFetchAction(localConversationId))
        assertEquals(PENDING, store.state.activeConversation.state)

        assertEquals(storage.deserialize(), store.state.activeConversation)

        // run conversation fetch
        executor.dispatchAll()

        /* 3. Conversation becomes 'pending' while fetching conversation token and identifier */
        assertEquals(ANONYMOUS, store.state.activeConversation.state)
        assertEquals(credentials.identifier, store.state.activeConversation.identifier)
        assertEquals(credentials.token, store.state.activeConversation.token)

        assertEquals(storage.deserialize(), store.state.activeConversation)
    }
}

private class MemoryStorage<T : Any> : Serializer, Deserializer {
    private var target: T? = null

    override fun serialize(obj: Any) {
        @Suppress("UNCHECKED_CAST")
        target = obj as T
    }

    override fun deserialize(): Any {
        return target ?: throw IllegalStateException("Storage is empty")
    }
}

private class MockConversationFetchService(
    private val credentials: ConversationCredentials,
    private val executor: Executor
) : ConversationFetchService {
    override fun fetchConversation(): Promise<ConversationCredentials> {
        val promise = AsyncPromise<ConversationCredentials>()
        executor.execute {
            promise.resolve(credentials)
        }
        return promise
    }

}