package apptentive.com.android.feedback.model

import apptentive.com.android.core.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.feedback.model.payloads.DevicePayload
import apptentive.com.android.feedback.utils.SensitiveDataUtils

internal data class Device(
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
        customData = customData.content,
        integrationConfig = integrationConfig.toPayload()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Device

        return !(
            osName != other.osName ||
                osVersion != other.osVersion ||
                osBuild != other.osBuild ||
                osApiLevel != other.osApiLevel ||
                manufacturer != other.manufacturer ||
                model != other.model ||
                board != other.board ||
                product != other.product ||
                brand != other.brand ||
                cpu != other.cpu ||
                device != other.device ||
                buildType != other.buildType ||
                buildId != other.buildId ||
                carrier != other.carrier ||
                currentCarrier != other.currentCarrier ||
                networkType != other.networkType ||
                bootloaderVersion != other.bootloaderVersion ||
                radioVersion != other.radioVersion ||
                localeCountryCode != other.localeCountryCode ||
                localeLanguageCode != other.localeLanguageCode ||
                localeRaw != other.localeRaw ||
                customData != other.customData ||
                integrationConfig != other.integrationConfig
            )
    }

    override fun hashCode(): Int {
        var result = osApiLevel
        result = 31 * result + utcOffset
        result = 31 * result + osName.hashCode()
        result = 31 * result + osVersion.hashCode()
        result = 31 * result + osBuild.hashCode()
        result = 31 * result + manufacturer.hashCode()
        result = 31 * result + model.hashCode()
        result = 31 * result + board.hashCode()
        result = 31 * result + product.hashCode()
        result = 31 * result + brand.hashCode()
        result = 31 * result + cpu.hashCode()
        result = 31 * result + device.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + buildType.hashCode()
        result = 31 * result + buildId.hashCode()
        result = 31 * result + (carrier?.hashCode() ?: 0)
        result = 31 * result + (currentCarrier?.hashCode() ?: 0)
        result = 31 * result + (networkType?.hashCode() ?: 0)
        result = 31 * result + (bootloaderVersion?.hashCode() ?: 0)
        result = 31 * result + (radioVersion?.hashCode() ?: 0)
        result = 31 * result + localeCountryCode.hashCode()
        result = 31 * result + localeLanguageCode.hashCode()
        result = 31 * result + localeRaw.hashCode()
        result = 31 * result + customData.hashCode()
        result = 31 * result + integrationConfig.hashCode()
        return result
    }
}
