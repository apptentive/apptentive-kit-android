package apptentive.com.android.feedback.utils

interface JsonSerializer {
    fun toJson(obj: Any) : String
}