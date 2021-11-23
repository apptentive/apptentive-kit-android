package apptentive.com.android.feedback.model

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.APP_RELEASE
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.DEVICE
import apptentive.com.android.feedback.ENGAGEMENT_DATA
import apptentive.com.android.feedback.PERSON
import apptentive.com.android.feedback.SDK
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.util.Log

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
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

    internal fun logConversation() {
        val dashLine = "-".repeat(20)
        Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
        Log.v(CONVERSATION, logReducedConversationObject())
        Log.v(DEVICE, device.toString())
        Log.v(PERSON, person.toString())
        Log.v(SDK, sdk.toString())
        Log.v(APP_RELEASE, appRelease.toString())
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
