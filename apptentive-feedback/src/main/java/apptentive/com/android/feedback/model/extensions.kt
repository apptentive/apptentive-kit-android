package apptentive.com.android.feedback.model

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.internal.StringSerializer

private val NullableStringSerializer = NullableSerializer(StringSerializer)

internal fun Encoder.encodeNullableString(str: String?) =
    encodeSerializableValue(NullableStringSerializer, str)

internal fun Decoder.decodeNullableString() =
    decodeSerializableValue(NullableStringSerializer)
