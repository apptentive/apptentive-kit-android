package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.Constants.buildHttpPath
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.generateUUID

@InternalUseOnly
class LogoutPayload(nonce: String = generateUUID()) : ConversationPayload(nonce) {

    //region Inheritance

    override fun getPayloadType() = PayloadType.Logout

    override fun getJsonContainer() = "delete"

    override fun getHttpMethod() = HttpMethod.DELETE

    override fun getHttpPath() = buildHttpPath("session")

    override fun getContentType(): MediaType = MediaType.applicationOctetStream

    override fun getDataBytes() = toJson().toByteArray()

    override fun getAttachmentDataBytes() = AttachmentData()

    //endregion

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LogoutPayload) return false
        if (!super.equals(other)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }

    //endregion

    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}