package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Result

@InternalUseOnly
interface MessageFetchService {
    fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    )
}
