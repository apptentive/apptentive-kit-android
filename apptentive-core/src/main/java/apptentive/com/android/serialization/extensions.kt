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
    TYPE_STRING,
    TYPE_NULL
}

object BasicTypeSerializer : TypeSerializer<Any?> {
    override fun encode(encoder: Encoder, value: Any?) {
        when (value) {
            is Boolean -> {
                encoder.encodeEnum(ValueType.TYPE_BOOLEAN)
                encoder.encodeBoolean(value as Boolean)
            }
            is Byte -> {
                encoder.encodeEnum(ValueType.TYPE_BYTE)
                encoder.encodeByte(value as Byte)
            }
            is Short -> {
                encoder.encodeEnum(ValueType.TYPE_SHORT)
                encoder.encodeShort(value as Short)
            }
            is Int -> {
                encoder.encodeEnum(ValueType.TYPE_INT)
                encoder.encodeInt(value as Int)
            }
            is Long -> {
                encoder.encodeEnum(ValueType.TYPE_LONG)
                encoder.encodeLong(value as Long)
            }
            is Float -> {
                encoder.encodeEnum(ValueType.TYPE_FLOAT)
                encoder.encodeFloat(value as Float)
            }
            is Double -> {
                encoder.encodeEnum(ValueType.TYPE_DOUBLE)
                encoder.encodeDouble(value as Double)
            }
            is Char -> {
                encoder.encodeEnum(ValueType.TYPE_CHAR)
                encoder.encodeChar(value as Char)
            }
            is String -> {
                encoder.encodeEnum(ValueType.TYPE_STRING)
                encoder.encodeString(value as String)
            }
            null -> encoder.encodeEnum(ValueType.TYPE_NULL)
            else -> throw NotImplementedError("Unsupported value type: ${value?.javaClass}")
        }
    }

    override fun decode(decoder: Decoder): Any? {
        val type = decoder.decodeEnum<ValueType>()
        return when (type) {
            ValueType.TYPE_BOOLEAN -> decoder.decodeBoolean()
            ValueType.TYPE_BYTE -> decoder.decodeByte()
            ValueType.TYPE_SHORT -> decoder.decodeShort()
            ValueType.TYPE_INT -> decoder.decodeInt()
            ValueType.TYPE_LONG -> decoder.decodeLong()
            ValueType.TYPE_FLOAT -> decoder.decodeFloat()
            ValueType.TYPE_DOUBLE -> decoder.decodeDouble()
            ValueType.TYPE_CHAR -> decoder.decodeChar()
            ValueType.TYPE_STRING -> decoder.decodeString()
            ValueType.TYPE_NULL -> null
        }
    }
}

object StringSerializer : TypeSerializer<String> {
    override fun encode(encoder: Encoder, value: String) = encoder.encodeString(value)

    override fun decode(decoder: Decoder) = decoder.decodeString()
}

object LongSerializer : TypeSerializer<Long> {
    override fun encode(encoder: Encoder, value: Long) = encoder.encodeLong(value)

    override fun decode(decoder: Decoder) = decoder.decodeLong()
}

fun Encoder.encodeMap(obj: Map<String, Any?>) {
    encodeMap(
        obj = obj,
        keyEncoder = StringSerializer,
        valueEncoder = BasicTypeSerializer
    )
}

fun <Key : Any, Value> Encoder.encodeMap(
    obj: Map<Key, Value>,
    keyEncoder: TypeEncoder<Key>,
    valueEncoder: TypeEncoder<Value>
) {
    encodeInt(obj.size)
    obj.forEach { pair ->
        keyEncoder.encode(this, pair.key)
        valueEncoder.encode(this, pair.value)
    }
}

fun Decoder.decodeMap(): Map<String, Any?> {
    return decodeMap(
        keyDecoder = StringSerializer,
        valueDecoder = BasicTypeSerializer
    )
}

fun <Key : Any, Value> Decoder.decodeMap(
    keyDecoder: TypeDecoder<Key>,
    valueDecoder: TypeDecoder<Value>
): MutableMap<Key, Value> {
    val size = decodeInt()
    if (size == 0) {
        return mutableMapOf()
    }

    val map = mutableMapOf<Key, Value>()
    for (i in 0 until size) {
        val key = keyDecoder.decode(this)
        val value = valueDecoder.decode(this)
        map[key] = value
    }
    return map
}

//endregion

inline fun <T> Encoder.encodeList(items: List<T>, callback: Encoder.(item: T)-> Unit) {
    encodeInt(items.size)
    items.forEach { callback(it) }
}

inline fun <T> Decoder.decodeList(callback: Decoder.() -> T): List<T> {
    val size = decodeInt()
    return List(size) { callback() }
}