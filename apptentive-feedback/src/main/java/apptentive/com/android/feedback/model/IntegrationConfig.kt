package apptentive.com.android.feedback.model

import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION
import org.json.JSONException

@InternalUseOnly
data class IntegrationConfig(
    var apptentive: IntegrationConfigItem? = null,
    var amazonAwsSns: IntegrationConfigItem? = null,
    var urbanAirship: IntegrationConfigItem? = null,
    var parse: IntegrationConfigItem? = null
) {
    fun toPayload(): Map<String, Any?>? {
        try {
            val ret = mutableMapOf<String, Any?>()
            if (apptentive != null) ret[INTEGRATION_APPTENTIVE_PUSH] = apptentive?.toPayload()
            if (amazonAwsSns != null) ret[INTEGRATION_AWS_SNS] = amazonAwsSns?.toPayload()
            if (urbanAirship != null) ret[INTEGRATION_URBAN_AIRSHIP] = urbanAirship?.toPayload()
            if (parse != null) ret[INTEGRATION_PARSE] = parse?.toPayload()
            return ret
        } catch (e: JSONException) {
            Log.e(PUSH_NOTIFICATION, "Exception when converting Integration Config", e)
        }
        return null
    }

    private fun IntegrationConfigItem.toPayload(): Map<String, Any?> {
        val ret = mutableMapOf<String, Any?>()
        val keys = contents.keys
        for (key in keys) {
            ret[key] = contents[key]
        }
        return ret
    }

    private companion object {
        private const val INTEGRATION_APPTENTIVE_PUSH = "apptentive_push"
        private const val INTEGRATION_AWS_SNS = "aws_sns"
        private const val INTEGRATION_URBAN_AIRSHIP = "urban_airship"
        private const val INTEGRATION_PARSE = "parse"
    }
}
