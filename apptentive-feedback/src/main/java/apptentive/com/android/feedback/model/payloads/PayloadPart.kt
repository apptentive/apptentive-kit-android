package apptentive.com.android.feedback.model.payloads

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.payload.MediaType
import java.io.ByteArrayOutputStream

internal interface PayloadPart {
    val contentType: MediaType get() = MediaType.applicationOctetStream
    val contentDisposition: String
        get() {
            return arrayOf("form-data", "name=\"${parameterName ?: "data"}\"", filename?.let { "filename=\"${filename}\"" })
                .mapNotNull { it }.joinToString(";")
        }
    val content: ByteArray get() = byteArrayOf()
    val filename: String? get() = null
    val parameterName: String? get() = null
}

