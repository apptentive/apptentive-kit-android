package apptentive.com.android.feedback.payload

import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.generateUUID

data class Payload(
    val nonce: String = generateUUID(),
    val type: PayloadType,
    val path: String,
    val method: HttpMethod,
    val mediaType: MediaType,
    val data: ByteArray
) {
    fun resolvePath(conversationId: String) = path.replace(":conversation_id", conversationId)

    override fun toString(): String {
        return "${javaClass.simpleName}(nonce=$nonce, type=$type, mediaType=$mediaType, data=${data.size} bytes)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Payload

        if (nonce != other.nonce) return false
        if (type != other.type) return false
        if (path != other.path) return false
        if (method != other.method) return false
        if (mediaType != other.mediaType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nonce.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}