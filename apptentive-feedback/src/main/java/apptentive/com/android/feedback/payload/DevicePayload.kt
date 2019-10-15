package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.util.generateUUID

// TODO: exclude this class from ProGuard
class DevicePayload(
    nonce: String,
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
    val integrationConfig: IntegrationConfigPayload
) : Payload(nonce) {
    companion object {
        fun fromDevice(device: Device) = DevicePayload(
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
            integrationConfig = IntegrationConfigPayload.fromIntegrationConfig(device.integrationConfig)
        )
    }
}

data class IntegrationConfigPayload(
    val apptentive: Map<String, Any?>? = null,
    val amazonAwsSns: Map<String, Any?>? = null,
    val urbanAirship: Map<String, Any?>? = null,
    val parse: Map<String, Any?>? = null
) {
    companion object {
        fun fromIntegrationConfig(config: IntegrationConfig) = IntegrationConfigPayload(
            apptentive = config.apptentive?.contents,
            amazonAwsSns = config.amazonAwsSns?.contents,
            urbanAirship = config.urbanAirship?.contents,
            parse = config.parse?.contents
        )
    }
}