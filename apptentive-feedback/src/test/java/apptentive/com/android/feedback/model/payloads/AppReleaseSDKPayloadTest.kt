package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.GenerateUUIDRule
import apptentive.com.android.feedback.MockTimeRule
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.toProperJson
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class AppReleaseSDKPayloadTest {
    @get:Rule
    val timeRule = MockTimeRule(currentTime = 1000.0, utcOffset = -18000)

    @get:Rule
    val uuidRule = GenerateUUIDRule()

    @Ignore("Passes locally. Failing on backend because of the UUID generation for session id.")
    @Test
    fun testAppReleaseAndSDKPayload() {
        val payload = AppReleaseAndSDKPayload(
            nonce = "nonce",
            sdkPlatform = "Android",
            sdkVersion = "1.0.0",
            debug = true,
            identifier = "apptentive.com.app",
            inheritingStyles = true,
            overridingStyles = false,
            targetSdkVersion = "30",
            minSdkVersion = "21",
            type = "SDK",
            versionCode = 1,
            versionName = "1.0.0"
        )
        val actualJson = payload.toJson()
        val expectedJson = toProperJson("{'app_release':{'sdk_platform':'Android','sdk_version':'1.0.0','debug':true,'identifier':'apptentive.com.app','inheriting_styles':true,'overriding_styles':false,'target_sdk_version':'30','min_sdk_version':'21','type':'SDK','version_code':1,'version_name':'1.0.0','session_id':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx','client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'nonce'}}")
        assertEquals(expectedJson, actualJson)

        val expected = PayloadData(
            nonce = "nonce",
            type = PayloadType.AppReleaseAndSDK,
            path = "/conversations/:conversation_id/app_release",
            method = HttpMethod.PUT,
            mediaType = MediaType.applicationJson,
            data = expectedJson.toByteArray()
        )
        val actual = payload.toPayloadData()
        assertEquals(expected, actual)
    }
}
