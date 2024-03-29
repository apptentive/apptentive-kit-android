package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PAYLOADS
import java.io.File

/**
 * A payload class to send messages.
 *
 * @param messageNonce - The nonce assigned to the message
 * @param body - Body of the message
 * @param hidden - Flag to determine whether the message should be hidden in the Message Center UI
 * @param automated - Flag to determine whether the message was sent by an automatic process
 */

@InternalUseOnly
data class MessagePayload(
    @Transient val messageNonce: String,
    @Transient var attachments: List<Message.Attachment>,
    val body: String?,
    val hidden: Boolean?,
    val automated: Boolean?,
    val customData: Map<String, Any?>? = null
) : ConversationPayload(messageNonce) {

    //region Inheritance

    override fun getPayloadType(): PayloadType = PayloadType.Message

    override fun getHttpMethod(): HttpMethod = HttpMethod.POST

    override fun getHttpPath(): String = Constants.buildHttpPath("messages")

    override fun getJsonContainer(): String = "message"

    // Omit JSON container because the Content-Disposition header identifies the payload type.
    override fun includeContainerKey(): Boolean = false

    // API requires most messages to be sent as multi-part (optional for text-only anonymous).
    override fun forceMultipart(): Boolean = true

    internal override fun getParts(embeddedToken: String?): List<PayloadPart> {
        val parts = super.getParts(embeddedToken).toMutableList()

        for (attachment in attachments) {
            try {
                val fileContents = File(attachment.localFilePath.orEmpty()).readBytes()
                parts.add(AttachmentPayloadPart(fileContents, attachment.contentType?.let { MediaType.parse(it) } ?: MediaType.applicationOctetStream, attachment.originalName))
            } catch (e: Exception) {
                Log.e(
                    PAYLOADS,
                    "Error reading Message Payload attachment: \"${attachment.localFilePath}\".",
                    e
                )
            }
        }

        return parts
    }

    //endregion
}
