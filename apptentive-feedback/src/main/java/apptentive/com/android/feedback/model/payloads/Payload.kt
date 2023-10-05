package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.EncryptedPayloadPart
import apptentive.com.android.feedback.payload.JSONPayloadPart
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadPart
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import java.io.ByteArrayOutputStream

@InternalUseOnly
abstract class Payload(
    val nonce: String,
) {
    // These are getter functions so that they don't show up in the toJson result.
    protected abstract fun getPayloadType(): PayloadType
    protected abstract fun getJsonContainer(): String?
    protected abstract fun getHttpMethod(): HttpMethod
    protected abstract fun getHttpPath(): String

    fun toJson(): String = JsonConverter.toJson(mapOf(getJsonContainer() to this))

    fun toPayloadData(credentialProvider: ConversationCredentialProvider): PayloadData {
        val parts = getParts()
        val boundary = "foo" // TODO: generate actual boundary string

        return PayloadData(
            nonce = nonce,
            type = getPayloadType(),
            tag = credentialProvider.conversationPath ?: "placeholder",
            token = credentialProvider.conversationToken,
            conversationId = credentialProvider.conversationId,
            isEncrypted = credentialProvider.payloadEncryptionKey != null,
            path = getHttpPath(),
            method = getHttpMethod(),
            mediaType = getContentType(parts, boundary, credentialProvider),
            data = getDataBytes(parts, boundary, credentialProvider)
        )
    }

    open fun getParts(): List<PayloadPart> {
        return listOf(JSONPayloadPart(toJsonObject(), getJsonContainer()))
    }

    private fun getContentType(parts: List<PayloadPart>, boundary: String, credentialProvider: ConversationCredentialProvider): MediaType? {
        val isEncrypted = credentialProvider.payloadEncryptionKey != null

        return when (parts.size) {
            0 -> null
            1 -> parts[0].contentType
            else -> if (isEncrypted) MediaType.multipartEncrypted(boundary)
            else MediaType.multipartMixed(boundary)
        }
    }

    private fun getDataBytes(parts: List<PayloadPart>, boundary: String, credentialProvider: ConversationCredentialProvider): ByteArray {
        var finalParts: List<PayloadPart> = parts
        val encryptionKey = credentialProvider.payloadEncryptionKey

        if (encryptionKey != null) {
            finalParts = parts.map {
                EncryptedPayloadPart(it, encryptionKey, parts.size > 1)
            }
        }

        return when (parts.size) {
            0 -> byteArrayOf()
            1 -> finalParts[0].content
            else -> assembleMultipart(finalParts, boundary)
        }
    }

    companion object {
        private const val LINE_END = "\r\n"
        private const val TWO_HYPHENS = "--"
    }

    private fun assembleMultipart(parts: List<PayloadPart>, boundary: String): ByteArray {
        val data = ByteArrayOutputStream()

        // Connect data to the request header.
        val headerPart = "${Payload.TWO_HYPHENS}$boundary${Payload.LINE_END}".toByteArray()
        data.write(headerPart)

        parts.forEach { part ->
            val attachmentsStart = "${Payload.LINE_END}${Payload.TWO_HYPHENS}$boundary${Payload.LINE_END}".toByteArray()
            data.write(attachmentsStart)

            val attachmentEnvelope = (
                    "Content-Disposition: form-data; " +
                            "name=\"file[]\"; " +
                            "filename=\"${part.filename}\"${Payload.LINE_END}" +
                            "Content-Type: ${part.contentType}${Payload.LINE_END}${Payload.LINE_END}"
                    ).toByteArray()

            data.write(attachmentEnvelope)
            data.write(part.content)
        }

        data.write(Payload.LINE_END.toByteArray())

        // No more data
        val endOfData = "${Payload.TWO_HYPHENS}$boundary${Payload.TWO_HYPHENS}".toByteArray()
        data.write(endOfData)

        Log.d(LogTags.PAYLOADS, "Total payload body bytes: %d", data.size())
        return data.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Payload) return false

        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        return nonce.hashCode()
    }
}
