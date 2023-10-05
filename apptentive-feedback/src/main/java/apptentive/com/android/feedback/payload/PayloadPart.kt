package apptentive.com.android.feedback.payload

import apptentive.com.android.encryption.EncryptionKey
import org.json.JSONObject

interface PayloadPart {
    val contentType: MediaType get() = MediaType.applicationOctetStream
    val contentDisposition: String
        get() {
            return arrayOf("form-data", "name=\"${parameterName ?: "data"}\"", filename?.let { "filename=\"${filename}\"" })
                .mapNotNull { it }.joinToString("; ")
        }
    val content: ByteArray get() = byteArrayOf()
    val filename: String? get() = null
    val parameterName: String? get() = null
}

class JSONPayloadPart(val json: JSONObject, val containerKey: String?): PayloadPart {
    override val contentType get() = MediaType.applicationJson
    override val filename get() = null
    override val parameterName get() = containerKey
    override val content get() = json.toString().toByteArray()
}

class AttachmentPayloadPart(override val content: ByteArray, override val contentType: MediaType, override val filename: String?): PayloadPart {
    override val parameterName get() = "file[]"
}

class EncryptedPayloadPart(val payloadPart: PayloadPart, val encryptionKey: EncryptionKey, val includeHeaders: Boolean) : PayloadPart {
    override val filename get() = payloadPart.filename
    override val parameterName get() = payloadPart.parameterName
    // TODO: override content
}