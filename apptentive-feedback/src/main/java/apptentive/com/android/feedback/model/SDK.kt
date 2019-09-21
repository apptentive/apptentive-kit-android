package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeNullableString

data class SDK(
    val version: String,
    val platform: String,
    val distribution: String,
    val distributionVersion: String,
    val programmingLanguage: String? = null,
    val authorName: String? = null,
    val authorEmail: String? = null
)

internal fun Encoder.encodeSDK(obj: SDK) {
    encodeString(obj.version)
    encodeString(obj.platform)
    encodeString(obj.distribution)
    encodeString(obj.distributionVersion)
    encodeNullableString(obj.programmingLanguage)
    encodeNullableString(obj.authorName)
    encodeNullableString(obj.authorEmail)
}

internal fun Decoder.decodeSDK(): SDK {
    return SDK(
        version = decodeString(),
        platform = decodeString(),
        distribution = decodeString(),
        distributionVersion = decodeString(),
        programmingLanguage = decodeNullableString(),
        authorName = decodeNullableString(),
        authorEmail = decodeNullableString()
    )
}