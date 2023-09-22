package apptentive.com.android.feedback.payload

import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.generateUUID

@InternalUseOnly
data class PayloadData(
    val nonce: String = generateUUID(),
    val tag: String,
    var token: String? = null,
    var conversationId: String? = null,
    var isEncrypted: Boolean = false,
    val type: PayloadType,
    val path: String,
    val method: HttpMethod,
    val mediaType: MediaType,
    var data: ByteArray,
    @Transient val attachmentData: AttachmentData = AttachmentData()
) {
    fun resolvePath(conversationId: String) = path.replace(":conversation_id", conversationId)

    override fun toString(): String {
        return "${javaClass.simpleName}(nonce=$nonce, type=$type, tag=$tag, token=$token, " +
            "conversationId = $conversationId, isEncrypted=$isEncrypted, mediaType=$mediaType, " +
            "dataFilePath=${attachmentData.dataFilePath}, data=${data.size} bytes)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PayloadData

        if (nonce != other.nonce) return false
        if (tag != other.tag) return false
        if (token != other.token) return false
        if (conversationId != other.conversationId) return false
        if (isEncrypted != other.isEncrypted) return false
        if (type != other.type) return false
        if (path != other.path) return false
        if (method != other.method) return false
        if (mediaType != other.mediaType) return false
        if (attachmentData != other.attachmentData) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nonce.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + tag.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + conversationId.hashCode()
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + attachmentData.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
