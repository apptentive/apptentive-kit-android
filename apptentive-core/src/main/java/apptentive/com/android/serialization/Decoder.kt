package apptentive.com.android.serialization

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
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
