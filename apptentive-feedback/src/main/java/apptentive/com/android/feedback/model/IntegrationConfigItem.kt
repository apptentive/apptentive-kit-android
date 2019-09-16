package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeMap
import apptentive.com.android.serialization.encodeMap

data class IntegrationConfigItem(val contents: Map<String, Any> = mapOf())

internal fun Encoder.encodeNullableIntegrationConfigItem(obj: IntegrationConfigItem?) {
    encodeBoolean(obj != null)
    if (obj != null) {
        encodeIntegrationConfigItem(obj)
    }
}

internal fun Encoder.encodeIntegrationConfigItem(obj: IntegrationConfigItem) {
    encodeMap(obj.contents)
}

internal fun Decoder.decodeNullableIntegrationConfigItem(): IntegrationConfigItem? =
    if (decodeBoolean()) decodeIntegrationConfigItem() else null

internal fun Decoder.decodeIntegrationConfigItem(): IntegrationConfigItem {
    return IntegrationConfigItem(contents = decodeMap())
}


