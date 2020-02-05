package apptentive.com.android.util

// FIXME: unit tests

class MissingKeyException(key: String) : RuntimeException("Missing key: $key")

@Throws(MissingKeyException::class)
fun Map<String, *>.getString(key: String): String = optString(key) ?: throw MissingKeyException(key)

fun Map<String, *>.optString(key: String, defaultValue: String? = null) =
    this[key]?.toString() ?: defaultValue

@Throws(MissingKeyException::class)
fun Map<String, *>.getInt(key: String): Int = optInt(key) ?: throw MissingKeyException(key)

fun Map<String, *>.optInt(key: String, defaultValue: Int? = null) =
    this[key] as? Int ?: defaultValue

@Throws(MissingKeyException::class)
fun Map<String, *>.getBoolean(key: String) = optBoolean(key) ?: throw MissingKeyException(key)

fun Map<String, *>.optBoolean(key: String, defaultValue: Boolean? = null) =
    this[key] as? Boolean ?: defaultValue

@Throws(MissingKeyException::class)
fun Map<String, *>.getMap(key: String): Map<String, *> =
    optMap(key) ?: throw MissingKeyException(key)

@Suppress("UNCHECKED_CAST")
fun Map<String, *>.optMap(key: String, defaultValue: Map<String, *>? = null) =
    this[key] as? Map<String, *> ?: defaultValue

@Throws(MissingKeyException::class)
fun Map<String, *>.getList(key: String): List<*> =
    optList(key) ?: throw MissingKeyException(key)

@Suppress("UNCHECKED_CAST")
fun Map<String, *>.optList(key: String, defaultValue: List<*>? = null) =
    this[key] as? List<*> ?: defaultValue