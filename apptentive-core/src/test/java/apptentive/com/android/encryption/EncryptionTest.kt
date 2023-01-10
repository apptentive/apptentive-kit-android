package apptentive.com.android.encryption

import android.os.Build.VERSION
import android.security.keystore.KeyProperties
import apptentive.com.android.TestCase
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Random
import javax.crypto.spec.SecretKeySpec

class EncryptionTest : TestCase() {

    private val encryption = EncryptionFactory.getEncryption(
        shouldEncryptStorage = true,
        oldEncryptionSetting = NotEncrypted,
        getEncryptionKey()
    )

    @Test
    fun testEmptyData() {
        val input = "".toByteArray()
        val encryptedBytes = encryption.encrypt(input)
        val decryptedData = encryption.decrypt(ByteArrayInputStream(encryptedBytes))

        Assert.assertEquals("", decryptedData.decodeToString())
    }

    @Test
    fun encryptSmallData() {
        val input = "Encryption is not fun"
        val encryptedBytes = encryption.encrypt(input.toByteArray())
        val decryptedBytes = encryption.decrypt(ByteArrayInputStream(encryptedBytes))

        Assert.assertEquals(input, decryptedBytes.decodeToString())
    }

    @Test
    fun testLargeData1() {
        val input = ByteArray(1024 * 21) // 21KB
        Random().nextBytes(input)

        val encryptedBytes = encryption.encrypt(input)
        val decryptedBytes = encryption.decrypt(ByteArrayInputStream(encryptedBytes))

        Assert.assertTrue(input.contentEquals(decryptedBytes))
    }

    @Test
    fun testLargeData2() {
        val input = ByteArray(1024 * 1024) // 1MB
        Random().nextBytes(input)

        val encryptedBytes = encryption.encrypt(input)
        val decryptedBytes = encryption.decrypt(ByteArrayInputStream(encryptedBytes))

        Assert.assertTrue(input.contentEquals(decryptedBytes))
    }

    @Test
    fun testLargeData3() {
        val input = ByteArray(1024 * 1024 * 10) // 10MB
        Random().nextBytes(input)

        val encryptedBytes = encryption.encrypt(input)
        val decryptedBytes = encryption.decrypt(ByteArrayInputStream(encryptedBytes))

        Assert.assertTrue(input.contentEquals(decryptedBytes))
    }

    @Test
    fun testNoOpEncryption() {
        var encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = false,
            oldEncryptionSetting = NotEncrypted
        )
        Assert.assertTrue(encryption is EncryptionNoOp)

        encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = true,
            oldEncryptionSetting = NotEncrypted
        )
        Assert.assertTrue(encryption is EncryptionNoOp)

        encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = false,
            oldEncryptionSetting = NoEncryptionStatus
        )
        Assert.assertTrue(encryption is EncryptionNoOp)

        encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = true,
            oldEncryptionSetting = NoEncryptionStatus,
            key = EncryptionKey.NO_OP
        )
        Assert.assertTrue(encryption is EncryptionNoOp)
    }

    @Test
    fun testAESEncryption() {
        setFinalStatic(VERSION::class.java.getField("SDK_INT"), 23)

        var encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = true,
            oldEncryptionSetting = Encrypted,
            key = getEncryptionKey()
        )
        Assert.assertTrue(encryption is AESEncryption23)

        encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = true,
            oldEncryptionSetting = NoEncryptionStatus,
            key = getEncryptionKey()
        )
        Assert.assertTrue(encryption is AESEncryption23)

        encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = false,
            oldEncryptionSetting = Encrypted,
            key = getEncryptionKey()
        )
        Assert.assertTrue(encryption is AESEncryption23)
    }

    @Throws(Exception::class)
    fun setFinalStatic(field: Field, newValue: Any?) {
        field.isAccessible = true
        val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
        field.set(null, newValue)
    }

    private fun getEncryptionKey(): EncryptionKey {
        val key =
            SecretKeySpec(hexToBytes("5C5361D08DA7AD6CD70ACEB572D387BB713A312DE8CE6128B8A42F62A7B381DB"), KeyProperties.KEY_ALGORITHM_AES)
        return EncryptionKey(key, "AES/CBC/PKCS7Padding")
    }

    private fun hexToBytes(hex: String): ByteArray {
        val length = hex.length
        val ret = ByteArray(length / 2)
        var i = 0
        while (i < length) {
            ret[i / 2] =
                (
                    (
                        ((hex[i].digitToIntOrNull(16) ?: (-1 shl 4)) + hex[i + 1].digitToIntOrNull(16)!!)
                        )
                    ).toByte()
            i += 2
        }
        return ret
    }

    interface BuildVersionAccessor {
        val SDK_INT: Int
    }
}
