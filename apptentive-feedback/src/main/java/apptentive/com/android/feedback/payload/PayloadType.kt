package apptentive.com.android.feedback.payload

enum class PayloadType {
    Person,
    Device,
    AppRelease,
    SDK,
    Message,
    Event,
    SurveyResponse;

    companion object {
        fun parse(value: String): PayloadType? {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                // TODO: log error message
                null
            }
        }
    }
}