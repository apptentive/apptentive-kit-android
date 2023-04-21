package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.GenerateUUIDRule
import apptentive.com.android.feedback.MockTimeRule
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.toProperJson
import junit.framework.TestCase.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class EventPayloadTest {
    @get:Rule
    val timeRule = MockTimeRule(currentTime = 1000.0, utcOffset = -18000)

    @get:Rule
    val uuidRule = GenerateUUIDRule()

    //region EventPayload

    @Ignore("Passes locally. Failing on backend because of the UUID generation for session id.")
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

        val expectedJson = toProperJson("{'event':{'label':'label','interaction_id':'interactionId','data':{'key':'value'},'custom_data':{'custom_key':'custom_value'},'session_id':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx','client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        val expected = PayloadData(
            nonce = "nonce",
            type = PayloadType.Event,
            path = "/conversations/:conversation_id/events",
            method = HttpMethod.POST,
            mediaType = MediaType.applicationJson,
            data = expectedJson.toByteArray()
        )
        val actual = payload.toPayloadData()
        assertEquals(expected, actual)
    }

    @Ignore("Passes locally. Failing on backend because of the UUID generation for session id.")
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

        val expected = toProperJson("{'event':{'label':'label','interaction_id':'interactionId','data':{'key':'value'},'custom_data':{'custom_key':'custom_value'},'session_id':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx','client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        val actual = payload.toJson()
        assertEquals(expected, actual)
    }

    @Ignore("Passes locally. Failing on backend because of the UUID generation for session id.")
    @Test
    fun testEventMissingJsonData() {
        val payload = EventPayload(
            nonce = "nonce",
            label = "label"
        )

        val expected = toProperJson("{'event':{'label':'label','session_id':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx','client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        val actual = payload.toJson()
        assertEquals(expected, actual)
    }

    @Test
    @Ignore
    fun testEventExtendedJsonData() {
    }

    //endregion
}
