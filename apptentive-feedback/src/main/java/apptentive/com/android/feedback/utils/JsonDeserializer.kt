package apptentive.com.android.feedback.utils

interface JsonDeserializer {
    fun <T> fromJson(json: String): T
}