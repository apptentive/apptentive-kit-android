package apptentive.com.android.util

import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.serialization.Encoder
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

fun encodeToByteArray(callback: (Encoder) -> Unit): ByteArray {
    val stream = ByteArrayOutputStream()
    val encoder = BinaryEncoder(DataOutputStream(stream))
    callback(encoder)
    return stream.toByteArray()
}