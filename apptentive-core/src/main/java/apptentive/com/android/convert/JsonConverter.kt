package apptentive.com.android.convert

import com.google.gson.Gson

/**
 * Represent an abstract JSON-serializer class.
 */
abstract class JsonConverter {
    /**
     * Constructs object of type T from a JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    inline fun <reified T> fromJson(json: String): T {
        return fromJson(json, T::class.java) as T
    }

    /**
     * Constructs object of a specific type from a JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    abstract fun fromJson(json: String, cls: Class<*>): Any

    /**
     * Serializes an object to JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    abstract fun toJson(obj: Any): String
}

/**
 * Concrete implementation based on gson library.
 */
class JsonConverterImpl : JsonConverter() {
    override fun fromJson(json: String, cls: Class<*>): Any {
        try {
            return gson.fromJson(json, cls)
        } catch (e: Exception) {
            throw JsonException(e)
        }
    }

    override fun toJson(obj: Any): String {
        try {
            return gson.toJson(obj)
        } catch (e: Exception) {
            throw JsonException(e)
        }
    }

    companion object {
        private val gson = Gson()
    }
}