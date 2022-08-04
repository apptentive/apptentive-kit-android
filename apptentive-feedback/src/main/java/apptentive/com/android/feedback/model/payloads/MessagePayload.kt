package apptentive.com.android.feedback.model.payloads

import android.net.Uri
import android.webkit.URLUtil
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.model.StoredFile
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.ImageUtil
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter
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
 * @param sender -  Data container class for Message Sender.
 * @param hidden - Flag to determine whether the message should be hidden in the Message Center UI
 * @param automated - Flag to determine whether the message was sent by an automatic process
 */

@InternalUseOnly
data class MessagePayload(
    @Transient val messageNonce: String,
    @Transient val boundary: String,
    @Transient var attachments: List<StoredFile>,
    val type: String,
    val body: String?,
    val sender: Sender?,
    val hidden: Boolean?,
    val automated: Boolean?,
    val customData: Map<String, Any?>? = null
) : ConversationPayload(messageNonce) {
    override fun getPayloadType(): PayloadType = PayloadType.Message

    override fun getContentType(): MediaType {
        return when (type) {
            Message.MESSAGE_TYPE_TEXT -> MediaType.applicationJson
            else -> MediaType.multipartUnauthenticated(boundary)
        }
    }

    override fun getHttpMethod(): HttpMethod = HttpMethod.POST

    override fun getHttpPath(): String = Constants.buildHttpPath("messages")

    override fun getJsonContainer(): String = "message"

    companion object {
        private const val LINE_END = "\r\n"
        private const val TWO_HYPHENS = "--"
    }

    /**
     * This is a multipart request. To accomplish this, we will create a data blog that is the entire contents
     * of the request after the request's headers. Each part of the body includes its own headers,
     * boundary, and data, but that is all rolled into one byte array to be stored pending sending.
     *
     * @return a Byte array that can be set on the payload request.
     */

    override fun getDataBytes(): ByteArray {
        if (type == Message.MESSAGE_TYPE_TEXT) return toJson().toByteArray()

        val data = ByteArrayOutputStream()

        // Connect data to the request header.
        val headerPart = "$TWO_HYPHENS$boundary$LINE_END".toByteArray()
        data.write(headerPart)

        // Write the message body out as the first part (it's okay if there is none).
        val textMessagePart = (
            "Content-Disposition: form-data; " +
                "name=\"message\"$LINE_END" +
                "Content-Type: {${MediaType.applicationJson}}$LINE_END$LINE_END" +
                JsonConverter.toJson(this)
            ).toByteArray()
        Log.v(PAYLOADS, "Writing text envelope: $textMessagePart")
        data.write(textMessagePart)

        // Then append attachments as other parts
        attachments.forEach { attachment ->
            data.write(getAttachmentByteStream(attachment).toByteArray())
        }
        data.write(LINE_END.toByteArray())

        // No more data
        val endOfData = "$TWO_HYPHENS$boundary$TWO_HYPHENS".toByteArray()
        data.write(endOfData)

        Log.d(PAYLOADS, "Total payload body bytes: %d", data.size())
        return data.toByteArray()
    }

    private fun getAttachmentByteStream(attachment: StoredFile): ByteArrayOutputStream {
        val attachmentStream = ByteArrayOutputStream()

        Log.v(PAYLOADS, "Starting to write an attachment part.")
        val attachmentsStart = "$LINE_END$TWO_HYPHENS$boundary$LINE_END".toByteArray()
        attachmentStream.write(attachmentsStart)

        val attachmentEnvelope = (
            "Content-Disposition: form-data; " +
                "name=\"file[]\"; " +
                "filename=\"${attachment.fileName}\"$LINE_END" +
                "Content-Type: ${attachment.mimeType}$LINE_END$LINE_END"
            ).toByteArray()

        Log.v(PAYLOADS, "Writing attachment envelope: $attachmentEnvelope")
        attachmentStream.write(attachmentEnvelope)

        val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext().getAppActivity()

        val inputPath =
            if (URLUtil.isContentUrl(attachment.sourceUriOrPath)) Uri.parse(attachment.sourceUriOrPath)
            else Uri.fromFile(File(attachment.sourceUriOrPath))
        val fileInputStream = activity.contentResolver.openInputStream(inputPath)

        try {
            requireNotNull(fileInputStream)
            if (FileUtil.isMimeTypeImage(attachment.mimeType)) {
                Log.v(PAYLOADS, "Appending image attachment.")
                ImageUtil.appendScaledDownImageToStream(
                    attachment.sourceUriOrPath,
                    fileInputStream,
                    attachmentStream
                )
            } else {
                Log.v(PAYLOADS, "Appending non-image attachment.")
                FileUtil.appendFileToStream(
                    fileInputStream,
                    attachmentStream
                )
            }
        } catch (e: Exception) {
            Log.e(
                PAYLOADS,
                "Error reading Message Payload attachment: \"${attachment.localFilePath}\".",
                e
            )
        } finally {
            FileUtil.ensureClosed(fileInputStream)
        }
        Log.v(PAYLOADS, "Writing attachment bytes: ${attachmentStream.size()}")
        return attachmentStream
    }
}
