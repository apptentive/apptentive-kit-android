package apptentive.com.android.feedback.utils

import java.math.BigInteger
import java.security.MessageDigest

// This is a one way encryption. Cannot be decrypted.
fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}
