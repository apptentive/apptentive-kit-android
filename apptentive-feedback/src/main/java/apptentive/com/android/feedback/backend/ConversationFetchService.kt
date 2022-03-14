package apptentive.com.android.feedback.backend

import androidx.annotation.Keep
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID

internal interface ConversationFetchService {
    fun fetchConversationToken(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        person: Person,
        callback: (Result<ConversationCredentials>) -> Unit
    )
}

@Keep
internal data class ConversationTokenRequestData private constructor(
    private val device: DeviceRequestData,
    private val appRelease: AppReleaseSdkRequestData,
    private val person: PersonRequestData
) {
    companion object {
        fun from(device: Device, sdk: SDK, appRelease: AppRelease, person: Person) =
            ConversationTokenRequestData(
                device = DeviceRequestData.from(device),
                appRelease = AppReleaseSdkRequestData.from(appRelease, sdk),
                person = PersonRequestData.from(person)
            )
    }
}

@Keep
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
            customData = device.customData.content,
            integrationConfig = IntegrationConfigRequestData.from(device.integrationConfig)
        )
    }
}

@Keep
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

@Keep
private data class AppReleaseSdkRequestData(
    val sdkNonce: String,
    val sdkAuthorEmail: String?,
    val sdkAuthorName: String?,
    val sdkDistribution: String?,
    val sdkDistributionVersion: String?,
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
    val minSdkVersion: String,
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
            minSdkVersion = appRelease.minSdkVersion,
            type = appRelease.type,
            versionCode = appRelease.versionCode,
            versionName = appRelease.versionName
        )
    }
}

@Keep
private data class PersonRequestData(
    val name: String?,
    val email: String?,
    val mparticleId: String?, // this is not a typo: the backend expects it as 'mparticle_id'
    val customData: Map<String, Any?>?
) {
    companion object {
        fun from(person: Person) = PersonRequestData(
            name = person.name,
            email = person.email,
            mparticleId = person.mParticleId,
            customData = person.customData.content
        )
    }
}

@Keep
internal data class ConversationCredentials(
    val id: String,
    val deviceId: String,
    val personId: String,
    val token: String,
    val encryptionKey: String
)
