package apptentive.com.android.feedback.utils

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.SensitiveDataKey
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTag
import org.json.JSONObject

internal object SensitiveDataUtils {
    internal var shouldSanitizeLogMessages = true
    private val sanitizedClasses: MutableMap<String, List<String>> = mutableMapOf()

    @JvmStatic fun hideIfSanitized(obj: Any?): String {
        return if (obj != null && shouldSanitizeLogMessages) Constants.REDACTED_DATA else obj.toString()
    }

    internal fun logWithSanitizeCheck(cls: Class<Any>, jsonObject: JSONObject): String {
        val jsonObjectToPrint: JSONObject? = if (shouldSanitizeLogMessages) {
            createSafeJsonObject(cls, jsonObject)
        } else jsonObject
        return String.format("%s %s", cls.simpleName, jsonObjectToPrint)
    }

    private fun createSafeJsonObject(cls: Class<Any>, jsonObject: JSONObject): JSONObject? {
        try {
            val sanitizedFields = getSanitizedClasses(cls)

            return JSONObject().apply {
                jsonObject.keys().forEach { key ->
                    val value = if (sanitizedFields.contains(key)) Constants.REDACTED_DATA else jsonObject[key]
                    put(key, value)
                }
            }
        } catch (e: Exception) {
            Log.e(LogTag(cls.simpleName), "Exception while creating safe json object: $e")
        }
        return null
    }

    private fun getSanitizedClasses(cls: Class<Any>): List<String> {
        return sanitizedClasses.getOrPut(
            cls.simpleName
        ) {
            cls.declaredFields.mapNotNull {
                if (it.isAnnotationPresent(SensitiveDataKey::class.java)) it.name.toSnakeCase()
                else null
            }
        }
    }

    /**
     * Uses a regex to find the positions before humps, inserting snakes, and then converts the whole
     * string to lowercase. The regex consists of two parts, the first one (?<=.) is a positive
     * look-behind saying that it must be preceded by a character, and the second part (?=\\p{Upper})
     * is using a positive look-ahead saying it must be followed by an uppercase character.
     *
     * Needed for sensitive fields comparison. Gson JSON serialization converts data to snake case
     * when and we need to compare with the camel case that comes from class field names.
     * @see apptentive.com.android.serialization.json.JsonConverter.gson
     */
    @VisibleForTesting
    internal fun String.toSnakeCase(): String {
        val humps = "(?<=.)(?=\\p{Upper})".toRegex()
        return replace(humps, "_").toLowerCase()
    }
}
