package apptentive.com.android.convert

import com.google.gson.Gson

/**
 * Utility class for JSON serialization
 */
internal object JsonConverter {
    private val gson = Gson()

    /**
     * Serializes an object to JSON string.
     * @throws [JsonException] if conversion fails.
     */
    @Throws(JsonException::class)
    inline fun <reified T> fromJson(json: String): T {
        return JsonConverter.fromJson(json, T::class.java) as T
    }

    /**
     * Serializes an object to JSON string.
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

