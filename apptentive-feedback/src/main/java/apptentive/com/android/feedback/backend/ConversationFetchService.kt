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
    val sdkNonce: String,
    val sdkAuthorEmail: String?,
    val sdkAuthorName: String?,
    val sdkDistribution: String,
    val sdkDistributionVersion: String,
    val sdkPlatform: String,
    val sdkProgrammingLanguage: String?,
    val sdkVersion: String,
    val nonce: String,
    val appStore: String?,
    val debug: Boolean,
    val identifier: String,
    val inheritingStyles: Boolean,
    val overridingStyles: Boolean,
    val targetSdkVersion: String,
    val type: String,
    val versionCode: Int,
    val versionName: String
) {
    companion object {
        fun from(appRelease: AppRelease, sdk: SDK) = AppReleaseSdkPayload(
            sdkNonce = generateUUID(),
            sdkAuthorEmail = sdk.authorEmail,
            sdkAuthorName = sdk.authorName,
            sdkDistribution = sdk.distribution,
            sdkDistributionVersion = sdk.distributionVersion,
            sdkPlatform = sdk.platform,
            sdkProgrammingLanguage = sdk.programmingLanguage,
            sdkVersion = sdk.version,
            nonce = generateUUID(),
            appStore = appRelease.appStore,
            debug = appRelease.debug,
            identifier = appRelease.identifier,
            inheritingStyles = appRelease.inheritStyle,
            overridingStyles = appRelease.overrideStyle,
            targetSdkVersion = appRelease.targetSdkVersion,
            type = appRelease.type,
            versionCode = appRelease.versionCode,
            versionName = appRelease.versionName
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