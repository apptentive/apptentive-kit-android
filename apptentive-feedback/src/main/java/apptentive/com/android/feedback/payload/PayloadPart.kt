package apptentive.com.android.feedback.payload

import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.model.payloads.Payload
import org.json.JSONObject
import java.io.ByteArrayOutputStream

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

class JSONPayloadPart(val json: String, val containerKey: String?): PayloadPart {
    override val contentType get() = MediaType.applicationJson
    override val filename get() = null
    override val parameterName get() = containerKey
    override val content get() = json.toByteArray()
}

class AttachmentPayloadPart(override val content: ByteArray, override val contentType: MediaType, override val filename: String?): PayloadPart {
    override val parameterName get() = "file[]"
}

class EncryptedPayloadPart(private val payloadPart: PayloadPart, val encryptionKey: EncryptionKey, private val includeHeaders: Boolean) : PayloadPart {
    override val filename get() = payloadPart.filename
    override val parameterName get() = payloadPart.parameterName
    override val content: ByteArray get() {
        val data = ByteArrayOutputStream()

        if (includeHeaders) {
            data.write("Content-Type: ${payloadPart.contentType}${Payload.LINE_END}".toByteArray())
            data.write("Content-Disposition: ${payloadPart.contentDisposition}${Payload.LINE_END}".toByteArray())
            data.write(Payload.LINE_END.toByteArray())
            data.write(payloadPart.content)
            data.write(Payload.LINE_END.toByteArray())
        }

        return payloadPart.content // TODO: encrypt it
    }
}