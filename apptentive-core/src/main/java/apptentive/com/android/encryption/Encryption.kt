package apptentive.com.android.encryption

import java.io.InputStream

/**
 * Represents and object for encrypting/decrypting on-device data storage.
 */

interface Encryption {
    /**
     * Encrypts an array of bytes and writes into the given OutputStream
     *
     * @param data - raw data to encrypt
     * @return an encrypted data
     */
    @Throws(EncryptionException::class)
    fun encrypt(data: ByteArray): ByteArray

    /**
     * Decrypts an array of bytes
     *
     * @param inputStream - inputStream with the data to decrypt
     * @return a decrypted data
     */
    @Throws(EncryptionException::class)
    fun decrypt(inputStream: InputStream): ByteArray

    /**
     * Decrypts an array of bytes
     *
     * @param data - raw ByteArray data to decrypt
     * @return a decrypted data
     */
    @Throws(EncryptionException::class)
    fun decrypt(data: ByteArray): ByteArray
}
