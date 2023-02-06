package apptentive.com.android.encryption

import apptentive.com.android.util.InternalUseOnly
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@InternalUseOnly
class EncryptionNoOp : Encryption {
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
