package apptentive.com.android.feedback.payload

class AppReleasePayload(
    nonce: String,
    val app_store: String?,
    val debug: Boolean,
    val identifier: String,
    val inheriting_styles: Boolean,
    val overriding_styles: Boolean,
    val target_sdk_version: String,
    val type: String,
    val version_code: Int,
    val version_name: String
) : Payload(nonce)