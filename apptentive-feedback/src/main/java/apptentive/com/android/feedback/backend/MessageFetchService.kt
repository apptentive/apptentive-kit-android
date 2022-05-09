package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.util.Result

internal interface MessageFetchService {
    fun getMessages(
        conversationToken: String,
        conversationId: String,
        callback: (Result<MessageList>) -> Unit
    )
}
