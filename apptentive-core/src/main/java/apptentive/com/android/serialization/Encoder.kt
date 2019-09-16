package apptentive.com.android.serialization

interface Encoder {
    fun encodeBoolean(value: Boolean)
    fun encodeByte(value: Byte)
    fun encodeShort(value: Short)
    fun encodeInt(value: Int)
    fun encodeLong(value: Long)
    fun encodeFloat(value: Float)
    fun encodeDouble(value: Double)
    fun encodeChar(value: Char)
    fun encodeString(value: String)
}

fun Encoder.encodeNullableString(value: String?) {
    encodeBoolean(value != null)
    if (value != null) {
        encodeString(value)
    }
}
