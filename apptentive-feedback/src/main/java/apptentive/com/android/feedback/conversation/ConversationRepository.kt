package apptentive.com.android.feedback.conversation

import apptentive.com.android.convert.Deserializer
import io.reactivex.Observable

internal interface ConversationRepository {
    fun getConversation(): Observable<Conversation>
}

internal class ConversationRepositoryImpl(
    private val diskRepository: ConversationRepository,
    private val networkRepository: ConversationRepository
) : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        val diskConversation = diskRepository.getConversation()
        val networkConversation = networkRepository.getConversation()
        return Observable
            .concat(diskConversation, networkConversation) // first, try loading from disk, then from the network
            .firstOrError() // emit first available item
            .toObservable()
    }
}

internal class DiskConversationRepository(private val deserializer: Deserializer) : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        return Observable.fromCallable { deserializer.deserialize() as Conversation }
    }
}

internal class NetworkConversationRepository(private val service: ConversationService) : ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        return service.getConversation()
    }
}