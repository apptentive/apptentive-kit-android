package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder

data class IntegrationConfig(
    val apptentive: IntegrationConfigItem? = null,
    val amazonAwsSns: IntegrationConfigItem? = null,
    val urbanAirship: IntegrationConfigItem? = null,
    val parse: IntegrationConfigItem? = null
)

internal fun Encoder.encodeIntegrationConfigItem(obj: IntegrationConfig) {
    encodeNullableIntegrationConfigItem(obj.apptentive)
    encodeNullableIntegrationConfigItem(obj.amazonAwsSns)
    encodeNullableIntegrationConfigItem(obj.urbanAirship)
    encodeNullableIntegrationConfigItem(obj.parse)
}
internal fun Decoder.decodeIntegrationConfig() : IntegrationConfig {
    return IntegrationConfig(
        apptentive = decodeNullableIntegrationConfigItem(),
        amazonAwsSns = decodeNullableIntegrationConfigItem(),
        urbanAirship = decodeNullableIntegrationConfigItem(),
        parse = decodeNullableIntegrationConfigItem()
    )
}
