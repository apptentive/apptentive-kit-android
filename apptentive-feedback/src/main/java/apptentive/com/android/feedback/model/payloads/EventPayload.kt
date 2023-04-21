package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants.buildHttpPath
import apptentive.com.android.feedback.model.SensitiveDataKey
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.generateUUID

@InternalUseOnly
class EventPayload(
    nonce: String = generateUUID(),
    val label: String,
    val interactionId: String? = null,
    val data: Map<String, Any?>? = null,
    @SensitiveDataKey val customData: Map<String, Any?>? = null,
    val extendedData: List<ExtendedData>? = null
) : ConversationPayload(nonce) {

    //region Inheritance

    override fun getPayloadType() = PayloadType.Event

    override fun getJsonContainer() = "event"

    override fun getHttpMethod() = HttpMethod.POST

    override fun getHttpPath() = buildHttpPath("events")

    override fun getContentType() = MediaType.applicationJson

    override fun getDataBytes() = toJson().toByteArray()

    override fun getAttachmentDataBytes() = AttachmentData()

    //endregion

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventPayload) return false
        if (!super.equals(other)) return false

        if (label != other.label) return false
        if (interactionId != other.interactionId) return false
        if (data != other.data) return false
        if (customData != other.customData) return false
        if (extendedData != other.extendedData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + (interactionId?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (customData?.hashCode() ?: 0)
        result = 31 * result + (extendedData?.hashCode() ?: 0)
        return result
    }

    //endregion

    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}

@InternalUseOnly
class ExtendedData
