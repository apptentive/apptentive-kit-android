package apptentive.com.android.feedback.backend

import apptentive.com.android.core.util.InternalUseOnly
import apptentive.com.android.core.util.Result
import apptentive.com.android.feedback.model.MessageList

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
