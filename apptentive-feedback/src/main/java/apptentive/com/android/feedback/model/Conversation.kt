package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject

data class Conversation(
    val localIdentifier: String,
    @SensitiveDataKey val conversationToken: String? = null,
    val conversationId: String? = null,
    val device: Device,
    val person: Person,
    val sdk: SDK,
    val appRelease: AppRelease,
    val engagementData: EngagementData = EngagementData(),
    val engagementManifest: EngagementManifest = EngagementManifest()
) {
    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }
}

val Conversation.hasConversationToken get() = this.conversationToken != null
