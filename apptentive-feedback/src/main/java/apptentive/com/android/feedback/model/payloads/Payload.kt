package apptentive.com.android.feedback.model.payloads

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
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
import java.util.Base64

@InternalUseOnly
abstract class Payload(
    val nonce: String,
) {
    // These are getter functions so that they don't show up in the toJson result.
    protected abstract fun getPayloadType(): PayloadType
    protected abstract fun getJsonContainer(): String?
    protected abstract fun getHttpMethod(): HttpMethod
    protected abstract fun getHttpPath(): String

    open fun toJson(): String = JsonConverter.toJson(mapOf(getJsonContainer() to this))

    fun toPayloadData(credentialProvider: ConversationCredentialProvider): PayloadData {
        val parts = getParts()
        val boundary = nonce.replace("-", "")

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
        return listOf(JSONPayloadPart(toJson(), getJsonContainer()))
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
        var maybeEncryptedParts: List<PayloadPart> = parts
        val encryptionKey = credentialProvider.payloadEncryptionKey

        if (encryptionKey != null) {
            maybeEncryptedParts = parts.map {
                EncryptedPayloadPart(it, encryptionKey, parts.size > 1)
            }
        }

        return when (parts.size) {
            0 -> byteArrayOf()
            1 -> maybeEncryptedParts[0].content
            else -> assembleMultipart(maybeEncryptedParts, boundary)
        }
    }

    companion object {
        internal const val LINE_END = "\r\n"
        internal const val TWO_HYPHENS = "--"
    }

    private fun assembleMultipart(parts: List<PayloadPart>, boundary: String): ByteArray {
        val data = ByteArrayOutputStream()

        parts.forEach { part ->
            data.write("${Payload.TWO_HYPHENS}$boundary${Payload.LINE_END}".toByteArray())
            data.write("Content-Type: ${part.contentType}${Payload.LINE_END}".toByteArray())
            data.write("Content-Disposition: ${part.contentDisposition}${Payload.LINE_END}".toByteArray())
            data.write(Payload.LINE_END.toByteArray())
            data.write(part.content)
            data.write(Payload.LINE_END.toByteArray())
        }

        // No more data
        val endOfData = "${Payload.TWO_HYPHENS}$boundary${Payload.TWO_HYPHENS}".toByteArray()
        data.write(endOfData)

        Log.d(LogTags.PAYLOADS, "Total payload body bytes: %d", data.size())

        val content = data.toByteArray()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.v(LogTags.PAYLOADS, "Body: ${Base64.getEncoder().encodeToString(content)}")
        }

        return content
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
