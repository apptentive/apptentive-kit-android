package apptentive.com.android.feedback.model

import apptentive.com.android.util.InternalUseOnly

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
) {
    /**
     * Data container class for Apptentive Message Center message.
     *
     * @param id - The server-side identifier for the message
     * @param nonce - The nonce assigned to the message
     * @param inbound - bool value to determine the message origin
     * @param createdAt - The message created time
     * @param body - Body of the message
     */

    data class Message(
        val customData: String?,
        val id: String?,
        val nonce: String?,
        val inbound: Boolean?,
        // val attachments: Attachment?,
        val createdAt: Double?,
        val sender: Sender?,
        val body: String?,
    )

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
}
