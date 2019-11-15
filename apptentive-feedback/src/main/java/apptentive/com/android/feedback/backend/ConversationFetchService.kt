package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.*
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID

interface ConversationFetchService {
    fun fetchConversationToken(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        callback: (Result<ConversationCredentials>) -> Unit
    )
}

// TODO: exclude this class from ProGuard
internal data class ConversationTokenRequestData private constructor(
    private val device: DeviceRequestData,
    private val appRelease: AppReleaseSdkRequestData
) {
    companion object {
        fun from(device: Device, sdk: SDK, appRelease: AppRelease) =
            ConversationTokenRequestData(
                device = DeviceRequestData.from(device),
                appRelease = AppReleaseSdkRequestData.from(appRelease, sdk)
            )
    }
}

// TODO: exclude this class from ProGuard
private class DeviceRequestData(
    val nonce: String,
    val uuid: String,
    val osName: String,
    val osVersion: String,
    val osBuild: String,
    val osApiLevel: String,
    val manufacturer: String,
    val model: String,
    val board: String,
    val product: String,
    val brand: String,
    val cpu: String,
    val device: String,
    val carrier: String?,
    val currentCarrier: String?,
    val networkType: String?,
    val buildType: String,
    val buildId: String,
    val bootloaderVersion: String?,
    val radioVersion: String?,
    val localeCountryCode: String,
    val localeLanguageCode: String,
    val localeRaw: String,
    val utcOffset: String,
    val advertiserId: String?,
    val customData: Map<String, Any?>?,
    val integrationConfig: IntegrationConfigRequestData
) {
    companion object {
        fun from(device: Device) = DeviceRequestData(
            nonce = generateUUID(),
            uuid = device.uuid,
            osName = device.osName,
            osVersion = device.osVersion,
            osBuild = device.osBuild,
            osApiLevel = device.osApiLevel.toString(),
            manufacturer = device.manufacturer,
            model = device.model,
            board = device.board,
            product = device.product,
            brand = device.brand,
            cpu = device.cpu,
            device = device.device,
            carrier = device.carrier,
            currentCarrier = device.currentCarrier,
            networkType = device.networkType,
            buildType = device.buildType,
            buildId = device.buildId,
            bootloaderVersion = device.bootloaderVersion,
            radioVersion = device.radioVersion,
            localeCountryCode = device.localeCountryCode,
            localeLanguageCode = device.localeLanguageCode,
            localeRaw = device.localeRaw,
            utcOffset = device.utcOffset.toString(),
            advertiserId = device.advertiserId,
            customData = device.customData.content,
            integrationConfig = IntegrationConfigRequestData.from(device.integrationConfig)
        )
    }
}

private data class IntegrationConfigRequestData(
    val apptentive: Map<String, Any?>? = null,
    val amazonAwsSns: Map<String, Any?>? = null,
    val urbanAirship: Map<String, Any?>? = null,
    val parse: Map<String, Any?>? = null
) {
    companion object {
        fun from(config: IntegrationConfig) = IntegrationConfigRequestData(
            apptentive = config.apptentive?.contents,
            amazonAwsSns = config.amazonAwsSns?.contents,
            urbanAirship = config.urbanAirship?.contents,
            parse = config.parse?.contents
        )
    }
}

// TODO: exclude this class from ProGuard
private data class AppReleaseSdkRequestData(
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
    val versionCode: VersionCode,
    val versionName: VersionName
) {
    companion object {
        fun from(appRelease: AppRelease, sdk: SDK) = AppReleaseSdkRequestData(
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
data class ConversationCredentials(
    val id: String,
    val deviceId: String,
    val personId: String,
    val token: String,
    val encryptionKey: String
)