package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Result

@InternalUseOnly
interface MessageCenterService {
    fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    )

    fun getAttachment(
        remoteUrl: String,
        callback: (Result<ByteArray>) -> Unit
    )
}
