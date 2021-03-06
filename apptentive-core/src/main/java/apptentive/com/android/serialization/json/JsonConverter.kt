package apptentive.com.android.serialization.json

import apptentive.com.android.util.InternalUseOnly
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject

/**
 * Utility class for JSON serialization
 */
@InternalUseOnly
object JsonConverter {
    private val gson: Gson by lazy {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

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
            val fixedJson = if (json.isBlank()) "{}" else json
            return gson.fromJson(fixedJson, type)
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

    /**
     * @return a [JSONObject] representation of passed in object (usually a data class)
     */
    fun Any.toJsonObject() = JSONObject(toJson(this))
}
