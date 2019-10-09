package apptentive.com.android.feedback.payload

class AppReleasePayload(
    nonce: String,
    val appStore: String?,
    val debug: Boolean,
    val identifier: String,
    val inheritingStyles: Boolean,
    val overridingStyles: Boolean,
    val targetSdkVersion: String,
    val type: String,
    val versionCode: Int,
    val versionName: String
) : Payload(nonce)