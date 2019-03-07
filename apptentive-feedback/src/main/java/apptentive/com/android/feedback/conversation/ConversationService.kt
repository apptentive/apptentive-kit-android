package apptentive.com.android.feedback.conversation

import apptentive.com.android.feedback.model.Conversation
import io.reactivex.Observable

interface ConversationService {
    fun fetchConversation(): Observable<Conversation>
}