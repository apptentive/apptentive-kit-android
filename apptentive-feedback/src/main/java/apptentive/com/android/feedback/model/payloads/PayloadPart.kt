package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.payload.MediaType

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
    val multipartHeaders: String
        get() {
            return "Content-Disposition: ${contentDisposition}\r\nContent-Type: ${contentType}\r\n"
        }
}
