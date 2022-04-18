package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.APP_RELEASE
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.LogTags.DEVICE
import apptentive.com.android.util.LogTags.ENGAGEMENT_DATA
import apptentive.com.android.util.LogTags.PERSON
import apptentive.com.android.util.LogTags.RANDOM_SAMPLING
import apptentive.com.android.util.LogTags.SDK

@InternalUseOnly
data class Conversation(
    val localIdentifier: String,
    @SensitiveDataKey val conversationToken: String? = null,
    val conversationId: String? = null,
    val device: Device,
    val person: Person,
    val sdk: SDK,
    val appRelease: AppRelease,
    val randomSampling: RandomSampling = RandomSampling(),
    val engagementData: EngagementData = EngagementData(),
    val engagementManifest: EngagementManifest = EngagementManifest()
) {
    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }

    internal fun logConversation() {
        val dashLine = "-".repeat(20)
        Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
        Log.v(CONVERSATION, logReducedConversationObject())
        Log.v(DEVICE, device.toString())
        Log.v(PERSON, person.toString())
        Log.v(SDK, sdk.toString())
        Log.v(APP_RELEASE, appRelease.toString())
        Log.v(RANDOM_SAMPLING, randomSampling.toString())
        Log.v(ENGAGEMENT_DATA, engagementData.toString())
        Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
    }

    private fun logReducedConversationObject(): String {
        return javaClass.simpleName +
            "(localIdentifier=\"$localIdentifier\", " +
            "conversationToken=\"${SensitiveDataUtils.hideIfSanitized(conversationToken)}\", " +
            "conversationID=\"$conversationId\")"
    }
}

internal val Conversation.hasConversationToken get() = this.conversationToken != null
