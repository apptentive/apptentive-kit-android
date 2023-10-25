package apptentive.com.android.feedback.payload

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64

interface PayloadPart {
    val contentType: String get() = MediaType.applicationOctetStream.toString()
    val contentDisposition: String
        get() {
            return arrayOf("form-data", "name=\"${parameterName ?: "data"}\"", filename?.let { "filename=\"${filename}\"" })
                .mapNotNull { it }.joinToString("; ")
        }
    val content: ByteArray get() = byteArrayOf()
    val filename: String? get() = null
    val parameterName: String? get() = null
}

class JSONPayloadPart(val json: String, private val containerKey: String?): PayloadPart {
    override val contentType get() = "${MediaType.applicationJson.toString()};charset=UTF-8"
    override val filename get() = null
    override val parameterName get() = containerKey
    override val content get() = json.toByteArray()
}

class AttachmentPayloadPart(override val content: ByteArray, override val contentType: String, override val filename: String?): PayloadPart {
    override val parameterName get() = "file[]"
}

class EncryptedPayloadPart(private val payloadPart: PayloadPart, val encryptionKey: EncryptionKey, private val includeHeaders: Boolean) : PayloadPart {
    override val filename get() = payloadPart.filename
    override val parameterName get() = payloadPart.parameterName
    override val content: ByteArray @RequiresApi(Build.VERSION_CODES.M)
    get() {
        val data = ByteArrayOutputStream()

        if (includeHeaders) { // Required when part of our multipart/encrypted encoding
            data.write("Content-Type: ${payloadPart.contentType}${Payload.LINE_END}".toByteArray())
            data.write("Content-Disposition: ${payloadPart.contentDisposition}${Payload.LINE_END}".toByteArray())
            data.write(Payload.LINE_END.toByteArray())
            data.write(payloadPart.content)
            data.write(Payload.LINE_END.toByteArray())
        }

        Log.v(LogTags.NETWORK, "plaintext: ${Base64.getEncoder().encodeToString(payloadPart.content)}")
        data.write(AESEncryption23(encryptionKey).encrypt(payloadPart.content))

        val result = data.toByteArray()

        Log.v(LogTags.NETWORK, "ciphertext: ${Base64.getEncoder().encodeToString(result)}")

        return result
    }
}