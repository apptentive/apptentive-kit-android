package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder

data class IntegrationConfigItem(val contents: Map<String, Any> = mapOf())

internal fun Encoder.encodeNullable(obj: IntegrationConfigItem?) {
    encodeBoolean(obj != null)
    if (obj != null) {
        encode(obj)
    }
}

internal fun Encoder.encode(obj: IntegrationConfigItem) {
    TODO()
}

internal fun Decoder.decodeNullableIntegrationConfigItem(): IntegrationConfigItem? =
    if (decodeBoolean()) decodeIntegrationConfigItem() else null

internal fun Decoder.decodeIntegrationConfigItem(): IntegrationConfigItem {
    TODO()
}


