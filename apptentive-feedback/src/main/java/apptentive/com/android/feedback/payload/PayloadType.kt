package apptentive.com.android.feedback.payload

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
enum class PayloadType {
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

    fun asString(): String {
        return name.lowercase()
    }
}
