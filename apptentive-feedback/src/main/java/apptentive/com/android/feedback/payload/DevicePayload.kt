package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.util.generateUUID

class DevicePayload(
    nonce: String,
    val uuid: String,
    val os_name: String,
    val os_version: String,
    val os_build: String,
    val os_api_level: String,
    val manufacturer: String,
    val model: String,
    val board: String,
    val product: String,
    val brand: String,
    val cpu: String,
    val device: String,
    val carrier: String?,
    val current_carrier: String?,
    val network_type: String?,
    val build_type: String,
    val build_id: String,
    val bootloader_version: String?,
    val radio_version: String?,
    val locale_country_code: String,
    val locale_language_code: String,
    val locale_raw: String,
    val utc_offset: String,
    val advertiser_id: String?,
    val custom_data: Map<String, Any?>?,
    val integration_config: IntegrationConfigPayload
) : Payload(nonce) {
    companion object {
        fun fromDevice(device: Device) = DevicePayload(
            nonce = generateUUID(),
            uuid = device.uuid,
            os_name = device.osName,
            os_version = device.osVersion,
            os_build = device.osBuild,
            os_api_level = device.osApiLevel.toString(),
            manufacturer = device.manufacturer,
            model = device.model,
            board = device.board,
            product = device.product,
            brand = device.brand,
            cpu = device.cpu,
            device = device.device,
            carrier = device.carrier,
            current_carrier = device.currentCarrier,
            network_type = device.networkType,
            build_type = device.buildType,
            build_id = device.buildId,
            bootloader_version = device.bootloaderVersion,
            radio_version = device.radioVersion,
            locale_country_code = device.localeCountryCode,
            locale_language_code = device.localeLanguageCode,
            locale_raw = device.localeRaw,
            utc_offset = device.utcOffset.toString(),
            advertiser_id = device.advertiserId,
            custom_data = device.customData.content,
            integration_config = IntegrationConfigPayload.fromIntegrationConfig(device.integrationConfig)
        )
    }
}

data class IntegrationConfigPayload(
    val apptentive: Map<String, Any>? = null,
    val amazonAwsSns: Map<String, Any>? = null,
    val urbanAirship: Map<String, Any>? = null,
    val parse: Map<String, Any>? = null
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