package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.payload.SidecarData
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
    protected abstract fun getJsonContainer(): String
    protected abstract fun getHttpMethod(): HttpMethod
    protected abstract fun getHttpPath(): String

    fun toJson(includeContainerKey: Boolean, embeddedToken: String?): String {
        return if (includeContainerKey) {
            val jsonContainer = mutableMapOf<String?, Any>(getJsonContainer() to this)
            embeddedToken?.let { jsonContainer["token"] = it }
            JsonConverter.toJson(jsonContainer)
        } else {
            val jsonObject = this.toJsonObject()
            embeddedToken?.let { jsonObject.put("token", it) }
            return jsonObject.toString()
        }
    }

    fun toPayloadData(credentialProvider: ConversationCredentialProvider): PayloadData {
        val encryptionKey = credentialProvider.payloadEncryptionKey
        var isEncrypted = false
        val parts: List<PayloadPart>
        val token: String?

        if (encryptionKey != null) {
            isEncrypted = true
            parts = getParts(credentialProvider.conversationToken).map {
                EncryptedPayloadPart(it, encryptionKey, forceMultipart())
            }
            token = "embedded"
        } else {
            parts = getParts(null)
            token = credentialProvider.conversationToken
        }

        var dataBytes = getDataBytes(parts, Payload.BOUNDARY)
        var attachmentData = SidecarData()

        // Use a sidecar file if dataBytes is more than 10KB.
        if (dataBytes.size > Payload.SQL_SIZE_LIMIT) {
            attachmentData = SidecarData(dataBytes)
            dataBytes = byteArrayOf()
        }

        return PayloadData(
            nonce = nonce,
            type = getPayloadType(),
            tag = credentialProvider.conversationPath,
            token = token,
            conversationId = credentialProvider.conversationId,
            isEncrypted = isEncrypted,
            path = getHttpPath(),
            method = getHttpMethod(),
            mediaType = getContentType(parts, Payload.BOUNDARY, isEncrypted),
            data = dataBytes,
            sidecarData = attachmentData
        )
    }

    internal open fun getParts(embeddedToken: String?): List<PayloadPart> {
        return listOf(JSONPayloadPart(toJson(includeContainerKey(), embeddedToken), getJsonContainer()))
    }

    internal open fun includeContainerKey(): Boolean = true // (multipart) message and logout payloads don't nest

    internal open fun forceMultipart(): Boolean = false // Message payloads set this to true

    internal open fun getContentType(parts: List<PayloadPart>, boundary: String, isEncrypted: Boolean): MediaType? {
        if (parts.isEmpty()) // Not used as currently there are no payloads without a body object.
            return null // (but would be correct to omit content type header)

        if (parts.size == 1 && !forceMultipart()) // Used for non-message requests.
            return parts[0].contentType // Return the content type of the first/only part.

        return if (isEncrypted)
            MediaType.multipartEncrypted(boundary) // Apptentive's own "multipart/encrypted" type.
        else
            MediaType.multipartMixed(boundary) // Standard multipart/mixed type.
    }

    internal open fun getDataBytes(parts: List<PayloadPart>, boundary: String): ByteArray {
        if (parts.isEmpty()) // Not used as currently there are no payloads without a body.
            return byteArrayOf() // (but would be correct to return empty body data)

        return if (parts.size == 1 && !forceMultipart()) // Used for non-message requests.
            parts[0].content // Return first/only part's body data.
        else
            assembleMultipart(parts, boundary) // For multipart, combine the parts.
    }

    companion object {
        internal const val LINE_END = "\r\n"
        internal const val TWO_HYPHENS = "--"
        internal const val BOUNDARY = "s16u0iwtqlokf4v9cpgne8a2amdrxz735hjby"
        internal const val SQL_SIZE_LIMIT = 10240

        internal fun assembleMultipart(parts: List<PayloadPart>, boundary: String): ByteArray {
            val data = ByteArrayOutputStream()

            parts.forEach { part ->
                data.write("${Payload.TWO_HYPHENS}$boundary${Payload.LINE_END}".toByteArray())
                data.write(part.multipartHeaders.toByteArray())
                data.write(Payload.LINE_END.toByteArray())
                data.write(part.content)
                data.write(Payload.LINE_END.toByteArray())
            }

            // No more data
            val endOfData = "${Payload.TWO_HYPHENS}$boundary${Payload.TWO_HYPHENS}".toByteArray()
            data.write(endOfData)

            val result = data.toByteArray()

            Log.d(LogTags.PAYLOADS, "Total payload body bytes: %d", result.size)

            return result
        }
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
