package apptentive.com.android.feedback.backend

import apptentive.com.android.core.network.Result
import apptentive.com.android.feedback.model.MessageList

internal interface MessageCenterService {
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
