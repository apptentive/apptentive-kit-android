package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.generateUUID

internal class AppReleaseAndSDKPayload(
    nonce: String = generateUUID(),
    val sdkAuthorEmail: String? = null,
    val sdkAuthorName: String? = null,
    val sdkDistribution: String? = null,
    val sdkDistributionVersion: String? = null,
    val sdkPlatform: String,
    val sdkProgrammingLanguage: String? = null,
    val sdkVersion: String,
    val appStore: String? = null,
    val debug: Boolean,
    val identifier: String,
    val inheritingStyles: Boolean,
    val overridingStyles: Boolean,
    val targetSdkVersion: String,
    val minSdkVersion: String,
    val type: String,
    val versionCode: Int,
    val versionName: String
) : ConversationPayload(nonce) {
    override fun getContentType(): MediaType = MediaType.applicationJson

    override fun getHttpMethod(): HttpMethod = HttpMethod.PUT

    override fun getHttpPath(): String = Constants.buildHttpPath("app_release")

    override fun getJsonContainer(): String = "app_release"

    override fun getPayloadType(): PayloadType = PayloadType.AppReleaseAndSDK

    override fun getDataBytes() = toJson().toByteArray()

    override fun getAttachmentDataBytes() = AttachmentData()

    companion object {
        fun buildPayload(sdk: SDK, appRelease: AppRelease): AppReleaseAndSDKPayload {
            return AppReleaseAndSDKPayload(
                sdkAuthorEmail = sdk.authorEmail,
                sdkAuthorName = sdk.authorName,
                sdkDistribution = sdk.distribution,
                sdkDistributionVersion = sdk.distributionVersion,
                sdkPlatform = sdk.platform,
                sdkVersion = sdk.version,
                appStore = appRelease.appStore,
                debug = appRelease.debug,
                identifier = appRelease.identifier,
                inheritingStyles = appRelease.inheritStyle,
                overridingStyles = appRelease.overrideStyle,
                targetSdkVersion = appRelease.targetSdkVersion,
                minSdkVersion = appRelease.minSdkVersion,
                type = appRelease.type,
                versionCode = appRelease.versionCode.toInt(),
                versionName = appRelease.versionName
            )
        }
    }
}
