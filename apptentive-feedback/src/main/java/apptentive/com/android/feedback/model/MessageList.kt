package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.model.Message.Status
import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.generateUUID
import java.io.File

/**
 * Data container class for Message Center message list.
 *
 * @param messages - Messages list returned from server [Message]
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
 * @param type - Text for text only. Compound if attachments are present.
 * @param sender - ID, name, and profile photo of sender from server [Sender]
 * @param body - `String` body of the message
 * @param attachments - Attachments either locally stored or from server [Message.Attachment]
 * @param messageStatus - Status of the message [Status]
 * @param inbound - `bool` value to determine the message origin. true = inbound to THE BACKEND. false = outbound from THE BACKEND.
 * @param hidden - `bool` value to determine if the message should be hidden in the UI. `String` or `null`
 * @param automated - `bool` value to determine if the message was sent by an automatic process. `String` or `null`
 * @param read - `bool` value to determine if the message was shown
 * @param createdAt - The message created time in seconds
 * @param groupTimestamp - Visually groups the first date of a specific date in the message list. `String` or `null`.
 * @param customData - Optional extra data sent with message
 */

@InternalUseOnly
data class Message(
    val id: String? = null,
    val nonce: String = generateUUID(),
    var type: String?,
    val sender: Sender?,
    val body: String?,
    var attachments: List<Attachment>? = emptyList(),
    var messageStatus: Status = Status.Unknown,
    val inbound: Boolean = false,
    val hidden: Boolean? = null, // true or null
    val automated: Boolean? = null, // true or null
    var read: Boolean? = true, // true or null
    var createdAt: TimeInterval = toSeconds(System.currentTimeMillis()), // Parity because server returns seconds
    var groupTimestamp: String? = null,
    val customData: Map<String, Any?>? = null
) {

    data class Attachment(
        var id: String? = null,

        // The mime type of the attachment
        var contentType: String? = null,

        /*
        * Bytes of saved file
        * Files from server are limited to 1MB (dashboard limitation)
        * Divide by 1024 to get kb. Divide again by 1024 to get mb
        */
        var size: Long = 0,

        /*
        * For outgoing attachment, this field is empty
        * For incoming attachment, this field is the full remote url to the attachment
        */
        var url: String? = null,

        /*
        * *Only valid for activity session*
        * The source image uri or source image full path
        */
        val sourceUriOrPath: String? = null,

        // The full path to the on-device cache file where the source image is copied to
        var localFilePath: String? = null,

        // Creation time of original file in seconds to match backend
        var creationTime: TimeInterval = toSeconds(System.currentTimeMillis()),

        // Will either be the actual file name (from original file or from remote), or `file.mimeTypeExtension`
        var originalName: String? = null,

        var isLoading: Boolean = false
    ) {
        fun hasLocalFile() = !localFilePath.isNullOrEmpty() && File(localFilePath.orEmpty()).exists()
    }

    fun toMessagePayload(): MessagePayload = MessagePayload(
        messageNonce = nonce,
        boundary = generateUUID().replace("-", ""),
        attachments = attachments.orEmpty(),
        type = type,
        body = body,
        sender = sender,
        automated = automated,
        hidden = hidden,
        customData = customData
    )

    enum class Status {
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

    companion object {
        const val MESSAGE_TYPE_TEXT = "Text"
        const val MESSAGE_TYPE_COMPOUND = "CompoundMessage"
    }
}

/**
 * Data container class for Support Representative Profile.
 * It is the info of the profile who has claimed the conversation on the dashboard.
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
