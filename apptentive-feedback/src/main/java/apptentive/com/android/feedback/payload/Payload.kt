package apptentive.com.android.feedback.payload

import apptentive.com.android.util.generateUUID

data class Payload(
    val nonce: String = generateUUID(),
    val type: PayloadType,
    val mediaType: MediaType,
    val data: ByteArray
)
{
    enum class PayloadType {
        Person,
        Device,
        AppRelease,
        SDK,
        Message,
        Event,
        SurveyResponse
    }
}