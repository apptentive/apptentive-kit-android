package apptentive.com.android.feedback.payload

internal enum class PayloadType {
    Person,
    Device,
    AppReleaseAndSDK,
    Message,
    Event,
    SurveyResponse,
    Logout;

    companion object {
        fun parse(value: String) = valueOf(value)
    }

    fun jsonContainer(): String {
        return when (this) {
            SurveyResponse -> "response"
            AppReleaseAndSDK -> "app_release"
            else -> name.lowercase()
        }
    }
}
