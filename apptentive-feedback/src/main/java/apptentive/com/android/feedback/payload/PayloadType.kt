package apptentive.com.android.feedback.payload

enum class PayloadType {
    Person,
    Device,
    AppReleaseAndSDK,
    Message,
    Event,
    SurveyResponse;

    companion object {
        fun parse(value: String) = valueOf(value)
    }
}
