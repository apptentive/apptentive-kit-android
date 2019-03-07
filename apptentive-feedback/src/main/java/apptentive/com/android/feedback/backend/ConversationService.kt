package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.network.HttpClient
import io.reactivex.Observable

interface ConversationService {
    fun fetchConversation(): Observable<Conversation>
}