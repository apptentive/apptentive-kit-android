package apptentive.com.android.feedback.payload

import org.junit.Assert.*
import org.junit.Test

class PayloadTypeTest {
    @Test
    fun parse() {
        val types = listOf(
            PayloadType.Person,
            PayloadType.Device,
            PayloadType.AppRelease,
            PayloadType.SDK,
            PayloadType.Message,
            PayloadType.Event,
            PayloadType.SurveyResponse,
            null
        )
        val values = listOf(
            "Person",
            "Device",
            "AppRelease",
            "SDK",
            "Message",
            "Event",
            "SurveyResponse",
            "MyPayload"
        )

        for (i in types.indices) {
            assertEquals(types[i], PayloadType.parse(values[i]))
        }
    }
}