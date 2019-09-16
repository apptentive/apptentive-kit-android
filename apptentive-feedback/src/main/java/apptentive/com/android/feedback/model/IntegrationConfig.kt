package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder

data class IntegrationConfig(
    val apptentive: IntegrationConfigItem? = null,
    val amazonAwsSns: IntegrationConfigItem? = null,
    val urbanAirship: IntegrationConfigItem? = null,
    val parse: IntegrationConfigItem? = null
)

internal fun Encoder.encode(obj: IntegrationConfig) {
    encodeNullable(obj.apptentive)
    encodeNullable(obj.amazonAwsSns)
    encodeNullable(obj.urbanAirship)
    encodeNullable(obj.parse)
}
internal fun Decoder.decodeIntegrationConfig() : IntegrationConfig {
    return IntegrationConfig(
        apptentive = decodeIntegrationConfigItem(),
        amazonAwsSns = decodeIntegrationConfigItem(),
        urbanAirship = decodeIntegrationConfigItem(),
        parse = decodeIntegrationConfigItem()
    )
}
