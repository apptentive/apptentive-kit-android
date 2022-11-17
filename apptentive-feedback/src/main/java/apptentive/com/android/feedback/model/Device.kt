package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.model.payloads.DevicePayload
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
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
    val localeCountryCode: String,
    val localeLanguageCode: String,
    val localeRaw: String,
    val utcOffset: Int,
    @SensitiveDataKey val customData: CustomData = CustomData(),
    val integrationConfig: IntegrationConfig = IntegrationConfig()
) {
    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }

    internal fun toDevicePayload(): DevicePayload = DevicePayload(
        osName = osName,
        osVersion = osVersion,
        osBuild = osBuild,
        osApiLevel = osApiLevel.toString(),
        manufacturer = manufacturer,
        model = model,
        board = board,
        product = product,
        brand = brand,
        cpu = cpu,
        device = device,
        uuid = uuid,
        buildType = buildType,
        buildId = buildId,
        carrier = carrier,
        currentCarrier = currentCarrier,
        networkType = networkType,
        bootloaderVersion = bootloaderVersion,
        radioVersion = radioVersion,
        localeCountryCode = localeCountryCode,
        localeLanguageCode = localeLanguageCode,
        localeRaw = localeRaw,
        utcOffset = utcOffset,
        customData = customData.content,
        integrationConfig = integrationConfig.toPayload()
    )
}
