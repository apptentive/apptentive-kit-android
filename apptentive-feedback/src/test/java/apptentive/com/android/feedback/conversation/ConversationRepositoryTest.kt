package apptentive.com.android.feedback.conversation

import io.reactivex.Observable
import org.junit.Assert.assertSame
import org.junit.Test
import java.lang.Exception

class ConversationRepositoryTest {
    @Test
    fun testLoadConversationFromNetwork() {
        val expected = Conversation()

        val diskRepository = MockConversationRepository(null)
        val networkRepository = MockConversationRepository(expected)
        val repository = ConversationRepositoryImpl(diskRepository, networkRepository)

        lateinit var actual: Conversation
        repository.getConversation().subscribe { conversation ->
            actual = conversation
        }

        assertSame(expected, actual)
    }

    @Test
    fun testLoadConversationFromDisk() {
        val expected = Conversation()

        val diskRepository = MockConversationRepository(expected)
        val networkRepository = FailConversationRepository
        val repository = ConversationRepositoryImpl(diskRepository, networkRepository)

        lateinit var actual: Conversation
        repository.getConversation().subscribe { conversation ->
            actual = conversation
        }

        assertSame(expected, actual)
    }
}

private class MockConversationRepository(private val conversation: Conversation?) : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        return if (conversation != null) {
            Observable.just(conversation)
        } else {
            Observable.empty()
        }
    }
}

private object FailConversationRepository : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        return Observable.error(Exception("Error"))
    }
}

private object ExceptionConversationRepository : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        throw Exception("Error")
    }
}