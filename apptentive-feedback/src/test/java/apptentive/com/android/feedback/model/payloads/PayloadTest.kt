package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.MockTimeRule
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.toProperJson
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class PayloadTest {
    @get:Rule
    val timeRule = MockTimeRule(currentTime = 1000.0, utcOffset = -18000)

    //region EventPayload

    @Test
    fun testEventPayloadData() {
        val payload = EventPayload(
            nonce = "nonce",
            label = "label",
            interactionId = "interactionId",
            data = mapOf<String, Any>(
                "key" to "value"
            ),
            customData = mapOf<String, Any>(
                "custom_key" to "custom_value"
            )
        )

        val expectedJson = toProperJson("{'event':{'label':'label','interaction_id':'interactionId','data':{'key':'value'},'custom_data':{'custom_key':'custom_value'},'client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        val expected = PayloadData(
            nonce = "nonce",
            type = PayloadType.Event,
            path = "/conversations/:conversation_id/events",
            method = HttpMethod.POST,
            mediaType = MediaType.applicationJson,
            data = expectedJson.toByteArray()
        )
        val actual = payload.toPayloadData()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testEventPayloadJson() {
        val payload = EventPayload(
            nonce = "nonce",
            label = "label",
            interactionId = "interactionId",
            data = mapOf<String, Any>(
                "key" to "value"
            ),
            customData = mapOf<String, Any>(
                "custom_key" to "custom_value"
            )
        )

        val expected = toProperJson("{'event':{'label':'label','interaction_id':'interactionId','data':{'key':'value'},'custom_data':{'custom_key':'custom_value'},'client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        val actual = payload.toJson()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testEventMissingJsonData() {
        val payload = EventPayload(
            nonce = "nonce",
            label = "label"
        )

        val expected = toProperJson("{'event':{'label':'label','client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        val actual = payload.toJson()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Ignore
    fun testEventExtendedJsonData() {
    }

    //endregion
}