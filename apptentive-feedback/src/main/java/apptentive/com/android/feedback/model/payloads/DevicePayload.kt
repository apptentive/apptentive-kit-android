package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants.buildHttpPath
import apptentive.com.android.feedback.model.SensitiveDataKey
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.generateUUID

internal class DevicePayload(
    nonce: String = generateUUID(),
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
    @SensitiveDataKey val customData: Map<String, Any?>? = null,
    @SensitiveDataKey val integrationConfig: Map<String, Any?>? = null,
) : ConversationPayload(nonce) {

    override fun getPayloadType() = PayloadType.Device
    override fun getJsonContainer() = "device"

    override fun getHttpMethod() = HttpMethod.PUT

    override fun getHttpPath() = buildHttpPath("device")

    override fun getContentType() = MediaType.applicationJson

    override fun getDataBytes() = toJson().toByteArray()

    override fun getAttachmentDataBytes() = AttachmentData()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is DevicePayload ||
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
                uuid != other.uuid ||
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
                integrationConfig != other.integrationConfig -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + osName.hashCode()
        result = 31 * result + osVersion.hashCode()
        result = 31 * result + osBuild.hashCode()
        result = 31 * result + osApiLevel.hashCode()
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

    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}
