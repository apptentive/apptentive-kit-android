package apptentive.com.android.serialization

//region Nullable String

fun Encoder.encodeNullableString(value: String?) {
    encodeBoolean(value != null)
    if (value != null) {
        encodeString(value)
    }
}

fun Decoder.decodeNullableString(): String? = if (decodeBoolean()) decodeString() else null

//endregion

//region Enum

fun Encoder.encodeEnum(value: Enum<*>) {
    encodeInt(value.ordinal)
}

inline fun <reified T : Enum<T>> Decoder.decodeEnum(): T {
    val ordinal = decodeInt()
    val values = enumValues<T>()
    return values[ordinal]
}

//endregion

//region Map

private enum class ValueType {
    TYPE_BOOLEAN,
    TYPE_BYTE,
    TYPE_SHORT,
    TYPE_INT,
    TYPE_LONG,
    TYPE_FLOAT,
    TYPE_DOUBLE,
    TYPE_CHAR,
    TYPE_STRING
}

fun Encoder.encodeMap(obj: Map<String, Any>) {
    encodeInt(obj.size)
    obj.forEach { pair ->
        encodeString(pair.key)
        when {
            pair.value is Boolean -> {
                encodeEnum(ValueType.TYPE_BOOLEAN)
                encodeBoolean(pair.value as Boolean)
            }
            pair.value is Byte -> {
                encodeEnum(ValueType.TYPE_BYTE)
                encodeByte(pair.value as Byte)
            }
            pair.value is Short -> {
                encodeEnum(ValueType.TYPE_SHORT)
                encodeShort(pair.value as Short)
            }
            pair.value is Int -> {
                encodeEnum(ValueType.TYPE_INT)
                encodeInt(pair.value as Int)
            }
            pair.value is Long -> {
                encodeEnum(ValueType.TYPE_LONG)
                encodeLong(pair.value as Long)
            }
            pair.value is Float -> {
                encodeEnum(ValueType.TYPE_FLOAT)
                encodeFloat(pair.value as Float)
            }
            pair.value is Double -> {
                encodeEnum(ValueType.TYPE_DOUBLE)
                encodeDouble(pair.value as Double)
            }
            pair.value is Char -> {
                encodeEnum(ValueType.TYPE_CHAR)
                encodeChar(pair.value as Char)
            }
            pair.value is String -> {
                encodeEnum(ValueType.TYPE_STRING)
                encodeString(pair.value as String)
            }
            else -> throw NotImplementedError("Unsupported value type: ${pair.value.javaClass}")
        }
    }
}

fun Decoder.decodeMap(): Map<String, Any> {
    val size = decodeInt()
    if (size == 0) {
        return mapOf()
    }

    val map = mutableMapOf<String, Any>()
    for (i in 0 until size) {
        val key = decodeString()
        val type = decodeEnum<ValueType>()
        val value: Any = when (type) {
            ValueType.TYPE_BOOLEAN -> decodeBoolean()
            ValueType.TYPE_BYTE -> decodeByte()
            ValueType.TYPE_SHORT -> decodeShort()
            ValueType.TYPE_INT -> decodeInt()
            ValueType.TYPE_LONG -> decodeLong()
            ValueType.TYPE_FLOAT -> decodeFloat()
            ValueType.TYPE_DOUBLE -> decodeDouble()
            ValueType.TYPE_CHAR -> decodeChar()
            ValueType.TYPE_STRING -> decodeString()
        }
        map[key] = value
    }
    return map
}

//endregion