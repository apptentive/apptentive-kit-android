package apptentive.com.android.feedback.conversation

import apptentive.com.android.network.HttpClient
import io.reactivex.Observable

internal interface ConversationFetchService {
    fun fetchConversation(): Observable<Conversation>
}

internal fun createConversationFetchService(httpClient: HttpClient): ConversationFetchService {
    return ConversationServiceImpl(httpClient)
}

private class ConversationServiceImpl(private val httpClient: HttpClient) : ConversationFetchService {
    override fun fetchConversation(): Observable<Conversation> {
        TODO("not implemented")
    }
}