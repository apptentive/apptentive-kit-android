package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeNullableString

data class Device(
    val osName: String,
    val osVersion: String,
    val osBuild: String,
    val osApiLevel: Int,
    val manufacturer: String,
    val model: String,
    val board: String,
    val product: String,
    val brand: String,
    val cpu: String,
    val device: String,
    val uuid: String,
    val buildType: String,
    val buildId: String,
    val carrier: String? = null,
    val currentCarrier: String? = null,
    val networkType: String? = null,
    val bootloaderVersion: String? = null,
    val radioVersion: String? = null,
    val localeCountryCode: String? = null,
    val localeLanguageCode: String? = null,
    val localeRaw: String? = null,
    val utcOffset: String? = null,
    val advertiserId: String? = null,
    val customData: CustomData = CustomData(),
    val integrationConfig: IntegrationConfig = IntegrationConfig()
)

internal fun Encoder.encodeDevice(obj: Device) {
    encodeString(obj.osName)
    encodeString(obj.osVersion)
    encodeString(obj.osBuild)
    encodeInt(obj.osApiLevel)
    encodeString(obj.manufacturer)
    encodeString(obj.model)
    encodeString(obj.board)
    encodeString(obj.product)
    encodeString(obj.brand)
    encodeString(obj.cpu)
    encodeString(obj.device)
    encodeString(obj.uuid)
    encodeString(obj.buildType)
    encodeString(obj.buildId)
    encodeNullableString(obj.carrier)
    encodeNullableString(obj.currentCarrier)
    encodeNullableString(obj.networkType)
    encodeNullableString(obj.bootloaderVersion)
    encodeNullableString(obj.radioVersion)
    encodeNullableString(obj.localeCountryCode)
    encodeNullableString(obj.localeLanguageCode)
    encodeNullableString(obj.localeRaw)
    encodeNullableString(obj.utcOffset)
    encodeNullableString(obj.advertiserId)
    encodeCustomData(obj.customData)
    encodeIntegrationConfigItem(obj.integrationConfig)
}

internal fun Decoder.decodeDevice() : Device {
    return Device(
        osName = decodeString(),
        osVersion = decodeString(),
        osBuild = decodeString(),
        osApiLevel = decodeInt(),
        manufacturer = decodeString(),
        model = decodeString(),
        board = decodeString(),
        product = decodeString(),
        brand = decodeString(),
        cpu = decodeString(),
        device = decodeString(),
        uuid = decodeString(),
        buildType = decodeString(),
        buildId = decodeString(),
        carrier = decodeNullableString(),
        currentCarrier = decodeNullableString(),
        networkType = decodeNullableString(),
        bootloaderVersion = decodeNullableString(),
        radioVersion = decodeNullableString(),
        localeCountryCode = decodeNullableString(),
        localeLanguageCode = decodeNullableString(),
        localeRaw = decodeNullableString(),
        utcOffset = decodeNullableString(),
        advertiserId = decodeNullableString(),
        customData = decodeCustomData(),
        integrationConfig = decodeIntegrationConfig()
    )
}