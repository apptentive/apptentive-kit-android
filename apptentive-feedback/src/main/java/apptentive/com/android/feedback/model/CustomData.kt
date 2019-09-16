package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeMap
import apptentive.com.android.serialization.encodeMap

data class CustomData(val content: Map<String, Any> = mapOf())

internal fun Encoder.encodeCustomData(obj: CustomData) {
    encodeMap(obj.content)
}

internal fun Decoder.decodeCustomData(): CustomData {
    val content = decodeMap()
    return CustomData(content)
}