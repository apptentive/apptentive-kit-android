package apptentive.com.android.util

import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal fun tryFormat(format: String, vararg args: Any?) = try {
    String.format(Locale.US, format, *args)
} catch (e: java.lang.Exception) {
    format
}

@OptIn(ExperimentalContracts::class)
fun String?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !isNullOrEmpty()
}

internal fun hexToBytes(hex: String): ByteArray {
    require(hex.length % 2 == 0) { "Hex string length must be even" }
    return hex.chunked(2) {
        it.toString().toInt(16).toByte()
    }.toByteArray()
}
