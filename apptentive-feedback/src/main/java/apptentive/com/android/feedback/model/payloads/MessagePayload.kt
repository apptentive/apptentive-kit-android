package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.AttachmentPayloadPart
import apptentive.com.android.feedback.payload.EncryptedPayloadPart
import apptentive.com.android.feedback.payload.JSONPayloadPart
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadPart
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
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

    override fun getParts(isEncrypted: Boolean, embeddedToken: String?): List<PayloadPart> {
        var parts: MutableList<PayloadPart> = mutableListOf(JSONPayloadPart(toJson(false, embeddedToken), getJsonContainer()))

        for (attachment in attachments) {
            val attachmentStream = ByteArrayOutputStream()
            try {
                retrieveAndWriteFileToStream(attachment, attachmentStream)
                parts.add(AttachmentPayloadPart(attachmentStream.toByteArray(), attachment.contentType?.let { it } ?: MediaType.applicationOctetStream.toString(), attachment.originalName))
            } finally {
                FileUtil.ensureClosed(attachmentStream)
            }
        }

        return parts
    }

    // Always send messages as multi-part
    override fun getContentType(parts: List<PayloadPart>, boundary: String, isEncrypted: Boolean): String? {
        return if (isEncrypted) MediaType.multipartEncrypted(boundary).toString()
        else MediaType.multipartMixed(boundary).toString()
    }

    // Always send messages as multi-part
    override fun getDataBytes(parts: List<PayloadPart>, boundary: String): ByteArray {
        return assembleMultipart(parts, boundary)
    }

    override fun maybeEncryptParts(
        parts: List<PayloadPart>,
        credentialProvider: ConversationCredentialProvider
    ): List<PayloadPart> {
        var maybeEncryptedParts: List<PayloadPart> = parts
        val encryptionKey = credentialProvider.payloadEncryptionKey

        if (encryptionKey != null) {
            maybeEncryptedParts = parts.map {
                EncryptedPayloadPart(it, encryptionKey, true)
            }
        }

        return maybeEncryptedParts
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
