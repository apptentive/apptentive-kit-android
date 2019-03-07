package apptentive.com.android.feedback.conversation

import apptentive.com.android.convert.Deserializer
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.utils.randomUUID
import io.reactivex.Observable
import org.junit.Assert.assertSame
import org.junit.Test

class ConversationRepositoryTest {
    @Test
    fun testLoadConversationFromNetwork() {
        val expected = Conversation(randomUUID())

        val diskRepository = createDiskRepository(null)
        val networkRepository = createNetworkRepository(expected)
        val repository = ConversationRepositoryImpl(diskRepository, networkRepository)

        lateinit var actual: Conversation
        repository.getConversation().subscribe { conversation ->
            actual = conversation
        }

        assertSame(expected, actual)
    }

    @Test
    fun testLoadConversationFromDisk() {
        val expected = Conversation(randomUUID())

        val diskRepository = createDiskRepository(expected)
        val networkRepository = FailConversationRepository
        val repository = ConversationRepositoryImpl(diskRepository, networkRepository)

        lateinit var actual: Conversation
        repository.getConversation().subscribe { conversation ->
            actual = conversation
        }

        assertSame(expected, actual)
    }

    private fun createDiskRepository(conversation: Conversation?): ConversationRepository {
        val deserializer = object : Deserializer {
            override fun deserialize(): Any {
                return conversation ?: throw Exception("Conversation can't be loaded")
            }
        }
        return DiskConversationRepository(deserializer)
    }

    private fun createNetworkRepository(conversation: Conversation?): ConversationRepository {
        val service = object : ConversationService {
            override fun fetchConversation(): Observable<Conversation> {
                return if (conversation != null) {
                    Observable.just(conversation)
                } else {
                    Observable.error(Exception("Conversation can't be fetched"))
                }
            }
        }
        return NetworkConversationRepository(service)
    }
}

private object FailConversationRepository : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        return Observable.error(Exception("Error"))
    }
}