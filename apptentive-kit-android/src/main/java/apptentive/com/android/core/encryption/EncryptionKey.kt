package apptentive.com.android.core.encryption

import android.security.keystore.KeyProperties
import apptentive.com.android.core.util.hexToBytes
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

internal data class EncryptionKey(val key: SecretKey? = null, val transformation: String = "") {
    companion object {
        val NO_OP: EncryptionKey = EncryptionKey()
    }
}

fun String.getKeyFromHexString(): SecretKey {
    return SecretKeySpec(hexToBytes(this), KeyProperties.KEY_ALGORITHM_AES)
}
