package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
abstract class Payload(val nonce: String) {
    protected abstract fun getPayloadType(): PayloadType
    protected abstract fun getJsonContainer(): String?
    protected abstract fun getHttpMethod(): HttpMethod
    protected abstract fun getHttpPath(): String
    protected abstract fun getContentType(): MediaType
    protected abstract fun getDataBytes(): ByteArray
    protected abstract fun getAttachmentDataBytes(): AttachmentData

    fun toJson(): String = JsonConverter.toJson(mapOf(getJsonContainer() to this))

    fun toPayloadData(credentialProvider: ConversationCredentialProvider) = PayloadData(
        nonce = nonce,
        type = getPayloadType(),
        tag = credentialProvider.conversationPath ?: "placeholder",
        token = credentialProvider.conversationToken,
        conversationId = credentialProvider.conversationId,
        isEncrypted = credentialProvider.payloadEncryptionKey != null,
        path = getHttpPath(),
        method = getHttpMethod(),
        mediaType = getFinalContentType(credentialProvider.payloadEncryptionKey != null),
        data = getFinalDataBytes(credentialProvider.payloadEncryptionKey),
        attachmentData = getAttachmentDataBytes()
    )

    private fun getFinalContentType(isEncrypted: Boolean): MediaType {
        return if (isEncrypted) {
            MediaType.applicationOctetStream // TODO: Handle multipart
        } else {
            getContentType()
        }
    }

    private fun getFinalDataBytes(payloadEncryptionKey: EncryptionKey?): ByteArray {
        return getDataBytes() // FIXME: Handle encryption
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
