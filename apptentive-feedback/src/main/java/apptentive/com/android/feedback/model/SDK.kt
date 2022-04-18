package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class SDK(
    val version: String,
    val platform: String,
    val distribution: String? = null,
    val distributionVersion: String? = null,
    val programmingLanguage: String? = null,
    @SensitiveDataKey val authorName: String? = null,
    @SensitiveDataKey val authorEmail: String? = null
) {
    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}
