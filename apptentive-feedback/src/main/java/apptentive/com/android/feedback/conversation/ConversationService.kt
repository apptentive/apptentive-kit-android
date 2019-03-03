package apptentive.com.android.feedback.conversation

import apptentive.com.android.network.HttpClient
import io.reactivex.Observable

internal interface ConversationService {
    fun getConversation(): Observable<Conversation>
}

internal fun createConversationService(httpClient: HttpClient): ConversationService {
    return ConversationServiceImpl(httpClient)
}

private class ConversationServiceImpl(private val httpClient: HttpClient) : ConversationService {
    override fun getConversation(): Observable<Conversation> {
        TODO("not implemented")
    }
}