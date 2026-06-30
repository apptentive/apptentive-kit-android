package apptentive.com.android.core.encryption

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal class EncryptionNoOp : Encryption {
    override fun decrypt(inputStream: InputStream): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        inputStream.use { input ->
            byteArrayOutputStream.use { output ->
                input.copyTo(output)
            }
        }
        return byteArrayOutputStream.toByteArray()
    }

    override fun encrypt(data: ByteArray): ByteArray {
        return data
    }

    override fun decrypt(data: ByteArray): ByteArray {
        val inputStream = ByteArrayInputStream(data)
        return decrypt(inputStream)
    }
}
