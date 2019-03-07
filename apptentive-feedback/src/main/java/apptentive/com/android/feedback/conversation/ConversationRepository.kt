package apptentive.com.android.feedback.conversation

import apptentive.com.android.convert.Deserializer
import apptentive.com.android.feedback.model.Conversation
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
        // errors are suppressed and observer completes as empty
        return Observable.create { emitter ->
            try {
                val conversation = deserializer.deserialize() as Conversation
                emitter.onNext(conversation)
            } catch (e: Exception) {
                // TODO: log error
            }
            emitter.onComplete()
        }
    }
}

internal class NetworkConversationRepository(private val service: ConversationService) :
    ConversationRepository {
    override fun getConversation(): Observable<Conversation> {
        return service.fetchConversation()
    }
}