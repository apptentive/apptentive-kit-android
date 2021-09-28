package apptentive.com.android.feedback.model.payloads

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.Constants.buildHttpPath
import apptentive.com.android.feedback.model.SensitiveDataKey
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.generateUUID

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
class PersonPayload(
    nonce: String = generateUUID(),
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val facebookId: String? = null,
    val phoneNumber: String? = null,
    val street: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val country: String? = null,
    val birthday: String? = null, // FIXME: make it Date
    @SensitiveDataKey val mParticleId: String? = null,
    @SensitiveDataKey val customData: Map<String, Any?>? = null,
) : ConversationPayload(nonce) {
    override fun getPayloadType() = PayloadType.Person
    override fun getJsonContainer() = "person"

    override fun getHttpMethod() = HttpMethod.PUT

    override fun getHttpPath() = buildHttpPath("person")

    override fun getContentType() = MediaType.applicationJson

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is PersonPayload ||
                id != other.id ||
                email != other.email ||
                name != other.name ||
                facebookId != other.facebookId ||
                phoneNumber != other.phoneNumber ||
                street != other.street ||
                city != other.city ||
                zip != other.zip ||
                country != other.country ||
                birthday != other.birthday ||
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
        result = 31 * result + (facebookId?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (street?.hashCode() ?: 0)
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (zip?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (birthday?.hashCode() ?: 0)
        result = 31 * result + (mParticleId?.hashCode() ?: 0)
        result = 31 * result + customData.hashCode()
        return result
    }

    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}
