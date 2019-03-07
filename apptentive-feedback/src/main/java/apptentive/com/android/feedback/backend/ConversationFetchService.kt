package apptentive.com.android.feedback.backend

import apptentive.com.android.concurrent.Promise

data class ConversationCredentials(val identifier: String, val token: String)

interface ConversationFetchService {
    fun fetchConversation(): Promise<ConversationCredentials>
}