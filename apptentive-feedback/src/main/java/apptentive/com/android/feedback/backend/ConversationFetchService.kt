package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.payload.DevicePayload
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID

interface ConversationFetchService {
    fun fetchConversationToken(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        callback: (Result<ConversationTokenFetchResponse>) -> Unit
    )
}

// TODO: exclude this class from ProGuard
internal data class ConversationTokenFetchBody(
    val device: DevicePayload,
    val appRelease: AppReleaseSdkPayload
) {
    companion object {
        fun from(device: Device, sdk: SDK, appRelease: AppRelease) =
            ConversationTokenFetchBody(
                device = DevicePayload.fromDevice(device),
                appRelease = AppReleaseSdkPayload.from(appRelease, sdk)
            )
    }
}

// TODO: exclude this class from ProGuard
data class AppReleaseSdkPayload(
    val sdk_nonce: String,
    val sdk_author_email: String?,
    val sdk_author_name: String?,
    val sdk_distribution: String,
    val sdk_distribution_version: String,
    val sdk_platform: String,
    val sdk_programming_language: String?,
    val sdk_version: String,
    val nonce: String,
    val app_store: String?,
    val debug: Boolean,
    val identifier: String,
    val inheriting_styles: Boolean,
    val overriding_styles: Boolean,
    val target_sdk_version: String,
    val type: String,
    val version_code: Int,
    val version_name: String
) {
    companion object {
        fun from(appRelease: AppRelease, sdk: SDK) = AppReleaseSdkPayload(
            sdk_nonce = generateUUID(),
            sdk_author_email = sdk.authorEmail,
            sdk_author_name = sdk.authorName,
            sdk_distribution = sdk.distribution,
            sdk_distribution_version = sdk.distributionVersion,
            sdk_platform = sdk.platform,
            sdk_programming_language = sdk.programmingLanguage,
            sdk_version = sdk.version,
            nonce = generateUUID(),
            app_store = appRelease.appStore,
            debug = appRelease.debug,
            identifier = appRelease.identifier,
            inheriting_styles = appRelease.inheritStyle,
            overriding_styles = appRelease.overrideStyle,
            target_sdk_version = appRelease.targetSdkVersion,
            type = appRelease.type,
            version_code = appRelease.versionCode,
            version_name = appRelease.versionName
        )
    }
}

// TODO: exclude this class from ProGuard
data class ConversationTokenFetchResponse(
    val id: String,
    val deviceId: String,
    val personId: String,
    val token: String,
    val encryptionKey: String
)