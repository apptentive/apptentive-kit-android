package apptentive.com.android.util

@InternalUseOnly
class MissingKeyException(key: String) : RuntimeException("Missing key: $key")

@Throws(MissingKeyException::class)
@InternalUseOnly
fun Map<String, *>.getString(key: String): String = optString(key) ?: throw MissingKeyException(key)

@InternalUseOnly
fun Map<String, *>.optString(key: String, defaultValue: String? = null) =
    this[key]?.toString() ?: defaultValue

@InternalUseOnly
@Throws(MissingKeyException::class)
fun Map<String, *>.getInt(key: String): Int {
    val value = this[key]
    if (value is Int) return value
    if (value is Double) return value.toInt()
    throw MissingKeyException(key)
}

@InternalUseOnly
fun Map<String, *>.optInt(key: String, defaultValue: Int = 0): Int {
    val value = this[key]
    if (value is Int) return value
    if (value is Double) return value.toInt()
    return defaultValue
}

@InternalUseOnly
@Throws(MissingKeyException::class)
fun Map<String, *>.getBoolean(key: String) = this[key] as? Boolean ?: throw MissingKeyException(key)

@InternalUseOnly
fun Map<String, *>.optBoolean(key: String, defaultValue: Boolean = false) =
    this[key] as? Boolean ?: defaultValue

@InternalUseOnly
@Throws(MissingKeyException::class)
fun Map<String, *>.getMap(key: String): Map<String, *> =
    optMap(key) ?: throw MissingKeyException(key)

@InternalUseOnly
@Suppress("UNCHECKED_CAST")
fun Map<String, *>.optMap(key: String, defaultValue: Map<String, *>? = null) =
    this[key] as? Map<String, *> ?: defaultValue

@InternalUseOnly
@Throws(MissingKeyException::class)
fun Map<String, *>.getList(key: String): List<*> =
    optList(key) ?: throw MissingKeyException(key)

@Suppress("UNCHECKED_CAST")
internal fun Map<String, *>.optList(key: String, defaultValue: List<*>? = null) =
    this[key] as? List<*> ?: defaultValue
