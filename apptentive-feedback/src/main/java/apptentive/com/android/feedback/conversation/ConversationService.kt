package apptentive.com.android.feedback.conversation

import apptentive.com.android.convert.Deserializer
import io.reactivex.Observable

internal interface ConversationService {
    fun getConversation(): Observable<Conversation>
}

internal class ConversationServiceImpl(
    private val cacheRepository: ConversationService,
    private val networkRepository: ConversationService
) : ConversationService {
    override fun getConversation(): Observable<Conversation> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

private class LocalConversationService(private val deserializer: Deserializer) : ConversationService {
    override fun getConversation(): Observable<Conversation> {
        return Observable.fromCallable { deserializer.deserialize() as Conversation }
    }
}

private class NetworkConversationService() : ConversationService {
    override fun getConversation(): Observable<Conversation> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}