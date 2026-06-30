package apptentive.com.android.core.encryption

import apptentive.com.android.core.LogTags.CRYPTOGRAPHY
import apptentive.com.android.util.Log

internal class EncryptionFactory {
    companion object {
        @Throws(EncryptionException::class)
        fun getEncryptionKey() = try {
            KeyResolverFactory.getKeyResolver().resolveKey()
        } catch (exception: EncryptionException) {
            Log.e(CRYPTOGRAPHY, "Key creation failure, cannot apply encryption", exception)
            EncryptionKey.NO_OP
        }

        fun getEncryption(shouldEncryptStorage: Boolean, oldEncryptionSetting: EncryptionStatus, key: EncryptionKey = getEncryptionKey()): Encryption =
            when {
                key.key == null -> EncryptionNoOp()

                shouldEncryptStorage && (oldEncryptionSetting == NoEncryptionStatus || oldEncryptionSetting == Encrypted) -> {
                    // previously encrypted or fresh launch, encrypt
                    AESEncryption23(key)
                }

                oldEncryptionSetting == Encrypted -> {
                    // may have encrypted storage, continue with AESEncryption
                    AESEncryption23(key)
                }

                oldEncryptionSetting == NotEncrypted -> {
                    // may have unencrypted storage continue with NoOp
                    EncryptionNoOp()
                }

                else -> EncryptionNoOp()
            }
    }
}
