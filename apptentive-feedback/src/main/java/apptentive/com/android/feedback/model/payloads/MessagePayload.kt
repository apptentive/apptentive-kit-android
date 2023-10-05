package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.AttachmentPayloadPart
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadPart
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PAYLOADS
import apptentive.com.android.util.isNotNullOrEmpty
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * A payload class to send messages.
 *
 * @param messageNonce - The nonce assigned to the message
 * @param body - Body of the message
 * @param sender -  Data container class for Message Sender.
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
    override fun getPayloadType(): PayloadType = PayloadType.Message

    override fun getHttpMethod(): HttpMethod = HttpMethod.POST

    override fun getHttpPath(): String = Constants.buildHttpPath("messages")

    override fun getJsonContainer(): String = "message"

    companion object {
        private const val LINE_END = "\r\n"
        private const val TWO_HYPHENS = "--"
    }

    override fun getParts(): List<PayloadPart> {
        var parts = super.getParts().toMutableList()

        for (attachment in attachments) {
            val attachmentStream = getAttachmentByteStream(attachment)
            try {
                parts.add(AttachmentPayloadPart(attachmentStream.toByteArray(), attachment.contentType?.let { MediaType.parse(it) } ?: MediaType.applicationOctetStream, attachment.originalName))
            } finally {
                FileUtil.ensureClosed(attachmentStream)
            }
        }

        return parts
    }

    /**
     * This is a multipart request. To accomplish this, we will create a data blob that is the entire contents
     * of the request after the request's headers. Each part of the body includes its own headers,
     * boundary, and data, but that is all rolled into one byte array to be stored pending sending.
     *
     * @return a Byte array that can be set on the payload request.
     */
    private fun saveDataBytes(): ByteArray {
        val data = ByteArrayOutputStream()

        // Connect data to the request header.
        val headerPart = "$TWO_HYPHENS$boundary$LINE_END".toByteArray()
        data.write(headerPart)

        // Write the message body out as the first part (it's okay if there is none).
        val textMessagePart = (
            "Content-Disposition: form-data; " +
                "name=\"message\"$LINE_END" +
                "Content-Type: ${MediaType.applicationJson};charset=UTF-8$LINE_END$LINE_END" +
                JsonConverter.toJson(this)
            ).toByteArray()
        Log.v(PAYLOADS, "Writing text envelope: $textMessagePart")
        data.write(textMessagePart)

        // Then append attachments as other parts
        attachments.forEach { attachment ->
            val attachmentStream = getAttachmentByteStream(attachment)
            try {
                data.write(attachmentStream.toByteArray())
            } finally {
                FileUtil.ensureClosed(attachmentStream)
            }
        }
        data.write(LINE_END.toByteArray())

        // No more data
        val endOfData = "$TWO_HYPHENS$boundary$TWO_HYPHENS".toByteArray()
        data.write(endOfData)

        Log.d(PAYLOADS, "Total payload body bytes: %d", data.size())
        return data.toByteArray()
    }

    private fun getAttachmentByteStream(attachment: Message.Attachment): ByteArrayOutputStream {
        val attachmentStream = ByteArrayOutputStream()

        retrieveAndWriteFileToStream(attachment, attachmentStream)
        Log.v(PAYLOADS, "Writing attachment bytes: ${attachmentStream.size()}")
        return attachmentStream
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
