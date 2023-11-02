package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.EncryptedPayloadPart
import apptentive.com.android.feedback.payload.JSONPayloadPart
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadPart
import apptentive.com.android.feedback.payload.PayloadType
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

    fun toJson(includeContainerKey: Boolean, embeddedToken: String?): String {
        return if (includeContainerKey) {
            val jsonContainer = mutableMapOf<String?, Any>(getJsonContainer() to this)
            embeddedToken?.let { jsonContainer["token"] = it }
            JsonConverter.toJson(jsonContainer)
        } else {
            var jsonObject = this.toJsonObject()
            embeddedToken?.let { jsonObject.put("token", it) }
            return jsonObject.toString()
        }
    }
    fun toPayloadData(credentialProvider: ConversationCredentialProvider): PayloadData {
        val isEncrypted = credentialProvider.payloadEncryptionKey != null
        val embeddedToken = if (isEncrypted) credentialProvider.conversationToken else null
        val parts = maybeEncryptParts(getParts(isEncrypted, embeddedToken), credentialProvider)
        val boundary = nonce.replace("-", "")
        var dataBytes = getDataBytes(parts, boundary)
        var attachmentData = AttachmentData()

        if (dataBytes.size > 10240) {
            attachmentData = AttachmentData(dataBytes)
            dataBytes = byteArrayOf()
        }

        return PayloadData(
            nonce = nonce,
            type = getPayloadType(),
            tag = credentialProvider.conversationPath ?: "placeholder",
            token = if (isEncrypted) "embedded" else credentialProvider.conversationToken,
            conversationId = credentialProvider.conversationId,
            isEncrypted = isEncrypted,
            path = getHttpPath(),
            method = getHttpMethod(),
            mediaType = getContentType(parts, boundary, isEncrypted),
            data = dataBytes,
            attachmentData = attachmentData
        )
    }

    open fun getParts(isEncrypted: Boolean, embeddedToken: String?): List<PayloadPart> {
        return listOf(JSONPayloadPart(toJson(true, embeddedToken), getJsonContainer()))
    }

    open fun shouldIncludeHeadersInEncryptedParts(): Boolean {
        return false
    }

    private fun maybeEncryptParts(parts: List<PayloadPart>, credentialProvider: ConversationCredentialProvider): List<PayloadPart> {
        var maybeEncryptedParts: List<PayloadPart> = parts
        val encryptionKey = credentialProvider.payloadEncryptionKey

        if (encryptionKey != null) {
            maybeEncryptedParts = parts.map {
                EncryptedPayloadPart(it, encryptionKey, shouldIncludeHeadersInEncryptedParts())
            }
        }

        return maybeEncryptedParts
    }

    open fun getContentType(parts: List<PayloadPart>, boundary: String, isEncrypted: Boolean): String? {
        return when (parts.size) {
            0 -> null
            1 -> parts[0].contentType
            else -> if (isEncrypted) MediaType.multipartEncrypted(boundary).toString()
            else MediaType.multipartMixed(boundary).toString()
        }
    }

    open fun getDataBytes(parts: List<PayloadPart>, boundary: String): ByteArray {
        return when (parts.size) {
            0 -> byteArrayOf()
            1 -> parts[0].content
            else -> assembleMultipart(parts, boundary)
        }
    }

    companion object {
        internal const val LINE_END = "\r\n"
        internal const val TWO_HYPHENS = "--"
    }

    protected fun assembleMultipart(parts: List<PayloadPart>, boundary: String): ByteArray {
        val data = ByteArrayOutputStream()

        parts.forEach { part ->
            data.write("${Payload.TWO_HYPHENS}$boundary${Payload.LINE_END}".toByteArray())
            data.write("Content-Disposition: ${part.contentDisposition}${Payload.LINE_END}".toByteArray())
            data.write("Content-Type: ${part.contentType}${Payload.LINE_END}".toByteArray())
            data.write(Payload.LINE_END.toByteArray())
            data.write(part.content)
            data.write(Payload.LINE_END.toByteArray())
        }

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
