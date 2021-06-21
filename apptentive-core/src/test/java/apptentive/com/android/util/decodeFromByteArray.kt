package apptentive.com.android.util

import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.Decoder
import java.io.ByteArrayInputStream
import java.io.DataInputStream

fun <T> decodeFromByteArray(array: ByteArray, callback: (Decoder) -> T): T {
    val stream = ByteArrayInputStream(array)
    val input = BinaryDecoder(DataInputStream(stream))
    return callback(input)
}
