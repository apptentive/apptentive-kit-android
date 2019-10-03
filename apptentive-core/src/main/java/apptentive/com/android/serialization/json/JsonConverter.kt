package apptentive.com.android.serialization.json

import com.google.gson.Gson

/**
 * Utility class for JSON serialization
 */
object JsonConverter {
    private val gson = Gson()

    /**
     * Serializes an object to JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    inline fun <reified T> fromJson(json: String): T {
        return fromJson(json, T::class.java) as T
    }

    /**
     * De-serializes an object from JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    fun fromJson(json: String, type: Class<*>): Any {
        try {
            return gson.fromJson(json, type)
        } catch (e: Exception) {
            throw JsonException(e)
        }
    }

    /**
     * De-serializes JSON string to a Map.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    fun toMap(json: String): Map<String, *> {
        try {
            @Suppress("UNCHECKED_CAST")
            return gson.fromJson(json, Map::class.java) as Map<String, *>
        } catch (e: Exception) {
            throw JsonException(e)
        }
    }

    /**
     * Serializes an object to JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    fun toJson(obj: Any): String {
        try {
            return gson.toJson(obj)
        } catch (e: Exception) {
            throw JsonException(e)
        }
    }
}

