package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.util.generateUUID

class EventPayload(
    nonce: String = generateUUID(),
    val label: String,
    val interactionId: String? = null,
    val data: Map<String, Any>? = null,
    val customData: Map<String, Any>? = null,
    val extendedData: List<ExtendedData>? = null
) : ConversationPayload(
    nonce = nonce
) {
    init {
        if (extendedData != null && extendedData.isNotEmpty()) {
            TODO("Extended data not supported yet")
        }
    }

    //region Inheritance

    override fun getPayloadType() = PayloadType.Event

    override fun getJsonContainer() = "event"

    override fun getHttpMethod() = HttpMethod.POST

    override fun getHttpPath() = "/conversations/:conversation_id/events"

    override fun getContentType() = MediaType.applicationJson

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
}

class ExtendedData {
}