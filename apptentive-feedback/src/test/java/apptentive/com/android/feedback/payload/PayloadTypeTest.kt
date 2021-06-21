package apptentive.com.android.feedback.payload

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
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
            PayloadType.SurveyResponse
        )
        val values = listOf(
            "Person",
            "Device",
            "AppRelease",
            "SDK",
            "Message",
            "Event",
            "SurveyResponse"
        )

        for (i in types.indices) {
            assertThat(types[i]).isEqualTo(PayloadType.parse(values[i]))
        }
    }

    @Test
    fun parseInvalidType() {
        try {
            PayloadType.parse("MyPayload")
            fail("Should not get there")
        } catch (_: IllegalArgumentException) {
        }
    }
}
