package apptentive.com.android.encryption

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CRYPTOGRAPHY
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.M)
@InternalUseOnly
class AESEncryption23(private val keyInfo: EncryptionKey) : Encryption {
    private val secureRandom = SecureRandom()

    private fun decryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance((keyInfo.transformation)).apply {
            init(Cipher.DECRYPT_MODE, keyInfo.key, IvParameterSpec(iv))
        }
    }

    private fun encryptCipherForIv(): Cipher {
        val iv = ByteArray(IV_LENGTH)
        secureRandom.nextBytes(iv)
        return Cipher.getInstance(keyInfo.transformation).apply {
            init(Cipher.ENCRYPT_MODE, keyInfo.key, IvParameterSpec(iv))
        }
    }

    override fun encrypt(data: ByteArray): ByteArray {
        val encryptCipher = encryptCipherForIv()
        val outputStream = ByteArrayOutputStream()

        outputStream.write(encryptCipher.iv.size)
        outputStream.write(encryptCipher.iv)

        val stream = CipherOutputStream(outputStream, encryptCipher)
        var offset = 0
        try {
            while (offset < data.size) {
                val blockLength: Int = min(CIPHER_CHUNK, data.size - offset)
                stream.write(data, offset, blockLength)
                offset += blockLength
            }
        } catch (e: Exception) {
            Log.e(CRYPTOGRAPHY, "Encryption failed ${e.message}", e)
            throw EncryptionException("Encryption failed", e)
        } finally {
            stream.close()
        }
        return outputStream.toByteArray()
    }

    override fun decrypt(data: ByteArray): ByteArray {
        val inputStream = ByteArrayInputStream(data)
        return decrypt(inputStream)
    }

    override fun decrypt(inputStream: InputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()
        inputStream.use { input ->
            val ivSize = input.read()
            val iv = ByteArray(ivSize)
            input.read(iv)
            val decryptCipher = decryptCipherForIv(iv)
            val cipherStream = CipherInputStream(input, decryptCipher)
            try {
                val chunkedData = ByteArray(CIPHER_CHUNK)
                var bytesRead: Int

                while (cipherStream.read(chunkedData).also { bytesRead = it } != -1) {
                    outputStream.write(chunkedData, 0, bytesRead)
                }
            } catch (e: Exception) {
                Log.e(CRYPTOGRAPHY, "Decryption failed. ${e.message}", e)
                throw EncryptionException("Decryption failed", e)
            } finally {
                cipherStream.close()
            }
        }
        return outputStream.toByteArray()
    }

    private companion object {
        const val CIPHER_CHUNK = 512
        const val IV_LENGTH = 16
    }
}
