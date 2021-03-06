package apptentive.com.android.serialization

import apptentive.com.android.util.InternalUseOnly

//region Nullable String

@InternalUseOnly
fun Encoder.encodeNullableString(value: String?) {
    encodeBoolean(value != null)
    if (value != null) {
        encodeString(value)
    }
}

@InternalUseOnly
fun Decoder.decodeNullableString(): String? = if (decodeBoolean()) decodeString() else null

//endregion

//region Enum

internal fun Encoder.encodeEnum(value: Enum<*>) {
    encodeInt(value.ordinal)
}

internal inline fun <reified T : Enum<T>> Decoder.decodeEnum(): T {
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

internal object BasicTypeSerializer : TypeSerializer<Any?> {
    override fun encode(encoder: Encoder, value: Any?) {
        when (value) {
            is Boolean -> {
                encoder.encodeEnum(ValueType.TYPE_BOOLEAN)
                encoder.encodeBoolean(value)
            }
            is Byte -> {
                encoder.encodeEnum(ValueType.TYPE_BYTE)
                encoder.encodeByte(value)
            }
            is Short -> {
                encoder.encodeEnum(ValueType.TYPE_SHORT)
                encoder.encodeShort(value)
            }
            is Int -> {
                encoder.encodeEnum(ValueType.TYPE_INT)
                encoder.encodeInt(value)
            }
            is Long -> {
                encoder.encodeEnum(ValueType.TYPE_LONG)
                encoder.encodeLong(value)
            }
            is Float -> {
                encoder.encodeEnum(ValueType.TYPE_FLOAT)
                encoder.encodeFloat(value)
            }
            is Double -> {
                encoder.encodeEnum(ValueType.TYPE_DOUBLE)
                encoder.encodeDouble(value)
            }
            is Char -> {
                encoder.encodeEnum(ValueType.TYPE_CHAR)
                encoder.encodeChar(value)
            }
            is String -> {
                encoder.encodeEnum(ValueType.TYPE_STRING)
                encoder.encodeString(value)
            }
            null -> encoder.encodeEnum(ValueType.TYPE_NULL)
            else -> throw NotImplementedError("Unsupported value type: ${value.javaClass}")
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

@InternalUseOnly
object StringSerializer : TypeSerializer<String> {
    override fun encode(encoder: Encoder, value: String) = encoder.encodeString(value)

    override fun decode(decoder: Decoder) = decoder.decodeString()
}

@InternalUseOnly
object LongSerializer : TypeSerializer<Long> {
    override fun encode(encoder: Encoder, value: Long) = encoder.encodeLong(value)

    override fun decode(decoder: Decoder) = decoder.decodeLong()
}

@InternalUseOnly
object DoubleSerializer : TypeSerializer<Double> {
    override fun encode(encoder: Encoder, value: Double) = encoder.encodeDouble(value)

    override fun decode(decoder: Decoder) = decoder.decodeDouble()
}

@InternalUseOnly
fun <Value> Encoder.encodeSet(obj: Set<Value>, valueEncoder: TypeEncoder<Value>) {
    encodeInt(obj.size)
    for (value in obj) {
        valueEncoder.encode(this, value)
    }
}

@InternalUseOnly
fun <Value> Decoder.decodeSet(valueDecoder: TypeDecoder<Value>): MutableSet<Value> {
    val size = decodeInt()
    if (size == 0) {
        return mutableSetOf()
    }

    val set = mutableSetOf<Value>()
    for (i in 0 until size) {
        val value = valueDecoder.decode(this)
        set.add(value)
    }
    return set
}

@InternalUseOnly
fun Encoder.encodeMap(obj: Map<String, Any?>) {
    encodeMap(
        obj = obj,
        keyEncoder = StringSerializer,
        valueEncoder = BasicTypeSerializer
    )
}

@InternalUseOnly
fun <Key : Any, Value> Encoder.encodeMap(
    obj: Map<Key, Value>,
    keyEncoder: TypeEncoder<Key>,
    valueEncoder: TypeEncoder<Value>
) {
    encodeInt(obj.size)
    for ((key, value) in obj) {
        keyEncoder.encode(this, key)
        valueEncoder.encode(this, value)
    }
}

@InternalUseOnly
fun Decoder.decodeMap(): Map<String, Any?> {
    return decodeMap(
        keyDecoder = StringSerializer,
        valueDecoder = BasicTypeSerializer
    )
}

@InternalUseOnly
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

@InternalUseOnly
inline fun <T> Encoder.encodeList(items: List<T>, callback: Encoder.(item: T) -> Unit) {
    encodeInt(items.size)
    for (item in items) {
        callback(item)
    }
}

@InternalUseOnly
inline fun <T> Decoder.decodeList(callback: Decoder.() -> T): List<T> {
    val size = decodeInt()
    return List(size) { callback() }
}
