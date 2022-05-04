package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.model.payloads.PersonPayload
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class Person(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    @SensitiveDataKey val mParticleId: String? = null,
    @SensitiveDataKey val customData: CustomData = CustomData()
) {

    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }

    internal fun toPersonPayload(): PersonPayload = PersonPayload(
        id = id,
        email = email,
        name = name,
        mParticleId = mParticleId,
        customData = customData.content
    )
}
