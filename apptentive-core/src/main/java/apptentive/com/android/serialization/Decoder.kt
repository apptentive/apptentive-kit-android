package apptentive.com.android.serialization

interface Decoder {
    fun decodeBoolean(): Boolean
    fun decodeByte(): Byte
    fun decodeShort(): Short
    fun decodeInt(): Int
    fun decodeLong(): Long
    fun decodeFloat(): Float
    fun decodeDouble(): Double
    fun decodeChar(): Char
    fun decodeString(): String
}

fun Decoder.decodeNullableString(): String? = if (decodeBoolean()) decodeString() else null