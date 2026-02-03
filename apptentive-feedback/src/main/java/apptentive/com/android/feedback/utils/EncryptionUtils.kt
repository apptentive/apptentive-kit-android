package apptentive.com.android.feedback.utils

import android.security.keystore.KeyProperties
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.encryption.KeyResolver23
import apptentive.com.android.encryption.KeyResolverFactory
import apptentive.com.android.util.InternalUseOnly
import java.math.BigInteger
import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

typealias SecretKeyBytes = ByteArray

// This is a one way encryption. Cannot be decrypted.
@InternalUseOnly
fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

internal fun SecretKey.toByteArray(): ByteArray =
    encoded

internal fun SecretKeyBytes.toEncryptionKey(): EncryptionKey =
    EncryptionKey(SecretKeySpec(this, KeyProperties.KEY_ALGORITHM_AES), KeyResolver23.getTransformation())

internal fun SecretKeyBytes.getEncryptionKey(user: String): EncryptionKey {
    val encryptedKey = KeyResolver23().resolveMultiUserWrapperKey(user)
    val encryption = AESEncryption23(encryptedKey)
    return EncryptionKey(
        SecretKeySpec(
            encryption.decrypt(this),
            KeyProperties.KEY_ALGORITHM_AES
        ),
        KeyResolver23.getTransformation()
    )
}

internal fun SecretKey.toSecretKeyBytes(user: String): SecretKeyBytes {
    val keyWrapperAlias = KeyResolverFactory.getKeyResolver().resolveMultiUserWrapperKey(user)
    return AESEncryption23(keyWrapperAlias).encrypt(this.toByteArray())
}
