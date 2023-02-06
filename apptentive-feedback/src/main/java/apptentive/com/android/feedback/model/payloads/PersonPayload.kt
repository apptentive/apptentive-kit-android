package apptentive.com.android.feedback.model.payloads

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.Constants.buildHttpPath
import apptentive.com.android.feedback.model.SensitiveDataKey
import apptentive.com.android.feedback.payload.AttachmentData
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.generateUUID

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class PersonPayload(
    nonce: String = generateUUID(),
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    @SensitiveDataKey val mParticleId: String? = null,
    @SensitiveDataKey val customData: Map<String, Any?>? = null,
) : ConversationPayload(nonce) {
    override fun getPayloadType() = PayloadType.Person
    override fun getJsonContainer() = "person"

    override fun getHttpMethod() = HttpMethod.PUT

    override fun getHttpPath() = buildHttpPath("person")

    override fun getContentType() = MediaType.applicationJson

    override fun getDataBytes() = toJson().toByteArray()

    override fun getAttachmentDataBytes() = AttachmentData()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is PersonPayload ||
                id != other.id ||
                email != other.email ||
                name != other.name ||
                mParticleId != other.mParticleId ||
                customData != other.customData -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (mParticleId?.hashCode() ?: 0)
        result = 31 * result + customData.hashCode()
        return result
    }

    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}
