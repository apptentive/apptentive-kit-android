package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeNullableString

data class AppRelease(
    val type: String,
    val identifier: String,
    val versionCode: Int = 0,
    val versionName: String,
    val targetSdkVersion: String,
    val debug: Boolean = false,
    val inheritStyle: Boolean = false,
    val overrideStyle: Boolean = false,
    val appStore: String? = null
)

internal fun Encoder.encodeAppRelease(obj: AppRelease) {
    encodeString(obj.type)
    encodeString(obj.identifier)
    encodeInt(obj.versionCode)
    encodeString(obj.versionName)
    encodeString(obj.targetSdkVersion)
    encodeBoolean(obj.debug)
    encodeBoolean(obj.inheritStyle)
    encodeBoolean(obj.overrideStyle)
    encodeNullableString(obj.appStore)
}

internal fun Decoder.decodeAppRelease(): AppRelease {
    return AppRelease(
        type = decodeString(),
        identifier = decodeString(),
        versionCode = decodeInt(),
        versionName = decodeString(),
        targetSdkVersion = decodeString(),
        debug = decodeBoolean(),
        inheritStyle = decodeBoolean(),
        overrideStyle = decodeBoolean(),
        appStore = decodeNullableString()
    )
}