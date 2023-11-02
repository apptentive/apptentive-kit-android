package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.payload.AttachmentPayloadPart
import apptentive.com.android.feedback.payload.JSONPayloadPart
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadPart
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PAYLOADS
import java.io.ByteArrayOutputStream
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

    override fun getParts(embeddedToken: String?): List<PayloadPart> {
        var parts: MutableList<PayloadPart> = super.getParts(embeddedToken).toMutableList()

        for (attachment in attachments) {
            val attachmentStream = ByteArrayOutputStream()
            try {
                retrieveAndWriteFileToStream(attachment, attachmentStream)
                parts.add(AttachmentPayloadPart(attachmentStream.toByteArray(), attachment.contentType?.let { MediaType.parse(it) } ?: MediaType.applicationOctetStream, attachment.originalName))
            } finally {
                FileUtil.ensureClosed(attachmentStream)
            }
        }

        return parts
    }

    //endregion

    }

    }


    private fun retrieveAndWriteFileToStream(attachment: Message.Attachment, attachmentStream: ByteArrayOutputStream) {
        try {
            Log.v(PAYLOADS, "Appending attachment.")
            attachmentStream.write(File(attachment.localFilePath.orEmpty()).readBytes())
        } catch (e: Exception) {
            Log.e(
                PAYLOADS,
                "Error reading Message Payload attachment: \"${attachment.localFilePath}\".",
                e
            )
        }
    }
}
