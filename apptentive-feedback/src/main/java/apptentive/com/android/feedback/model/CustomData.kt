package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder

data class CustomData(val content: Map<String, Any?> = mapOf())

internal fun Encoder.encode(obj: CustomData) {
    TODO()
}

internal fun Decoder.decodeCustomData() : CustomData {
    TODO()
}