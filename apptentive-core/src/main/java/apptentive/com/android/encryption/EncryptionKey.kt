package apptentive.com.android.encryption

import apptentive.com.android.util.InternalUseOnly
import javax.crypto.SecretKey

@InternalUseOnly
data class EncryptionKey(val key: SecretKey? = null, val transformation: String = "") {
    companion object {
        val NO_OP: EncryptionKey = EncryptionKey()
    }
}
