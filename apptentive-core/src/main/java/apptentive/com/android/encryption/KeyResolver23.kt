package apptentive.com.android.encryption

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants.CRYPTO_KEY_ALIAS
import apptentive.com.android.platform.SharedPrefConstants.SDK_CORE_INFO
import java.security.KeyStore
import java.util.UUID
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
internal class KeyResolver23 : KeyResolver {
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private val androidProxy by lazy { DependencyProvider.of<AndroidSharedPrefDataStore>() }

    @Throws(EncryptionException::class)
    override fun resolveKey(): EncryptionKey {
        return EncryptionKey(getKey(), getTransformation())
    }

    @Throws(EncryptionException::class)
    private fun getKey(): SecretKey {
        val keyAlias = androidProxy.getString(SDK_CORE_INFO, CRYPTO_KEY_ALIAS)
        val existingKey = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry

        return if (existingKey?.secretKey == null) {
            val keyAlias = KEY_ALIAS + UUID.randomUUID()
            androidProxy.putString(SDK_CORE_INFO, CRYPTO_KEY_ALIAS, keyAlias)
            createKey(keyAlias)
        } else {
            existingKey.secretKey
        }
    }

    private fun createKey(keyAlias: String): SecretKey {
        return try {
            KeyGenerator.getInstance(ALGORITHM, KEYSTORE_PROVIDER).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setKeySize(KEY_LENGTH)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setUserAuthenticationRequired(false)
                        .setRandomizedEncryptionRequired(false) // we need that to make our custom IV work
                        .build()
                )
            }.generateKey()
        } catch (exception: Exception) {
            throw EncryptionException("Exception thrown at the key creation", exception)
        }
    }

    private fun getTransformation() = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    private companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        const val KEY_ALIAS = "apptentive-crypto-key-SDK"
        const val KEY_LENGTH = 256
    }
}
