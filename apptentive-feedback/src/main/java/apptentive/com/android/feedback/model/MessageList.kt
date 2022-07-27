package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.model.Message.Status
import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.generateUUID

/**
 * Data container class for Message Center message list.
 *
 * @param endsWith - Last downloaded Message ID.
 * @param hasMore - boolean value to determine if there are more messages to fetch from the server
 */

@InternalUseOnly
data class MessageList(
    val messages: List<Message>?,
    val endsWith: String?,
    val hasMore: Boolean?,
)

/**
 * Data container class for Apptentive Message Center message.
 *
 * @param id - The server-side identifier for the message
 * @param nonce - The nonce assigned to the message
 * @param body - Body of the message
 * @param messageStatus - Status of the message [Status]
 * @param inbound - bool value to determine the message origin. true = inbound to THE BACKEND. false = outbound from THE BACKEND.
 * @param hidden - bool value to determine if the message should be hidden in the UI
 * @param automated - bool value to determine if the message was sent by an automatic process
 * @param createdAt - The message created time
 */

@InternalUseOnly
data class Message(
    val id: String? = null,
    val nonce: String = generateUUID(),
    // TODO find if type is needed at all
    var type: String,
    // val attachments: Attachment?,
    val sender: Sender?,
    val body: String?,
    var messageStatus: Status = Status.Unknown,
    val inbound: Boolean = false,
    val hidden: Boolean = false,
    val automated: Boolean = false,
    val customData: Map<String, Any?>? = null,
    var createdAt: TimeInterval = toSeconds(System.currentTimeMillis()), // Parity because server returns seconds
    var groupTimestamp: String? = null
) {
    fun toMessagePayload(): MessagePayload = MessagePayload(
        messageNonce = nonce,
        type = type,
        body = body ?: "",
        sender = sender,
        hidden = hidden,
        customData = customData
    )

    enum class Status {
        Draft, // A draft message
        Sending, // Queued for sending
        Sent, // Posted to the server
        Failed, // Permanently failed
        Saved, // Incoming message
        Unknown;

        companion object {
            fun parse(state: String): Status {
                try {
                    return Status.valueOf(state)
                } catch (e: IllegalArgumentException) {
                    Log.e(MESSAGE_CENTER, "Error parsing unknown Message.status: $state")
                }
                return Unknown
            }
        }
    }
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
