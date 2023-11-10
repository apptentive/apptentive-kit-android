package apptentive.com.android.encryption

import android.os.Build
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.hexToBytes
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@InternalUseOnly
data class EncryptionKey(val key: SecretKey? = null, val transformation: String = "") {
    companion object {
        val NO_OP: EncryptionKey = EncryptionKey()
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun String.getKeyFromHexString(): SecretKey {
    return SecretKeySpec(hexToBytes(this), KeyProperties.KEY_ALGORITHM_AES)
}
