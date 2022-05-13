package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.generateUUID

/**
 * Data container class for Message Center message list.
 *
 * @param endsWith - Last downloaded Message ID.
 * @param hasMore - boolean value to determine if there are more messages to fetch from the server
 */

@InternalUseOnly
data class MessageList(
    val messageList: List<Message>?,
    val endsWith: String?,
    val hasMore: Boolean?,
)

/**
 * Data container class for Apptentive Message Center message.
 *
 * @param id - The server-side identifier for the message
 * @param nonce - The nonce assigned to the message
 * @param inbound - bool value to determine the message origin
 * @param createdAt - The message created time
 * @param body - Body of the message
 */

@InternalUseOnly
data class Message(
    val customData: String? = null,
    val id: String? = null,
    val nonce: String = generateUUID(),
    val type: String,
    // val attachments: Attachment?,
    val sender: Sender?,
    val body: String?,
    val inbound: Boolean = false,
    val hidden: Boolean = false,
    val automated: Boolean = false
) {
    fun toMessagePayload(): MessagePayload = MessagePayload(
        nonce = nonce,
        type = type,
        body = body ?: "",
        sender = sender,
    )
}

/**
 * Data container class for Message Sender.
 *
 * @param id - The sender id
 * @param name - The sender's name
 * @param profilePhoto - Url to the Sender's profile photo if available
 */

data class Sender(
    val id: String?,
    val name: String?,
    val profilePhoto: String?,
)
