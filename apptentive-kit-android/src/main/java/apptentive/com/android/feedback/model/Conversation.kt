package apptentive.com.android.feedback.model

import apptentive.com.android.core.serialization.json.JsonConverter.toJsonObject
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogLevel
import apptentive.com.android.core.util.LogTags.APP_RELEASE
import apptentive.com.android.core.util.LogTags.CONFIGURATION
import apptentive.com.android.core.util.LogTags.CONVERSATION
import apptentive.com.android.core.util.LogTags.DEVICE
import apptentive.com.android.core.util.LogTags.ENGAGEMENT_DATA
import apptentive.com.android.core.util.LogTags.PERSON
import apptentive.com.android.core.util.LogTags.RANDOM_SAMPLING
import apptentive.com.android.core.util.LogTags.SDK
import apptentive.com.android.feedback.utils.SensitiveDataUtils

internal data class Conversation(
    val localIdentifier: String,
    @SensitiveDataKey val conversationToken: String? = null,
    val conversationId: String? = null,
    val device: Device,
    val person: Person,
    val sdk: SDK,
    val appRelease: AppRelease,
    val sdkStatus: SDKStatus = SDKStatus(),
    val randomSampling: RandomSampling = RandomSampling(),
    val engagementData: EngagementData = EngagementData(),
    val engagementManifest: EngagementManifest = EngagementManifest()
) {
    override fun toString(): String {
        return SensitiveDataUtils.logWithSanitizeCheck(javaClass, toJsonObject())
    }

    internal fun logConversation() {
        if (Log.canLog(LogLevel.Verbose)) {
            val dashLine = "-".repeat(20)
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
            Log.v(CONVERSATION, logReducedConversationObject())
            Log.v(DEVICE, device.toString())
            Log.v(PERSON, person.toString())
            Log.v(SDK, sdk.toString())
            Log.v(APP_RELEASE, appRelease.toString())
            Log.v(CONFIGURATION, sdkStatus.toString())
            Log.v(RANDOM_SAMPLING, randomSampling.toString())
            Log.v(ENGAGEMENT_DATA, engagementData.toString())
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
        }
    }

    internal fun logConfiguration() {
        if (Log.canLog(LogLevel.Verbose)) {
            val dashLine = "-".repeat(20)
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
            Log.v(CONFIGURATION, sdkStatus.toString())
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
        }
    }

    internal fun logEngagementData() {
        if (Log.canLog(LogLevel.Verbose)) {
            val dashLine = "-".repeat(20)
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
            Log.v(ENGAGEMENT_DATA, engagementData.toString())
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
        }
    }

    internal fun logPerson() {
        if (Log.canLog(LogLevel.Verbose)) {
            val dashLine = "-".repeat(20)
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
            Log.v(PERSON, person.toString())
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
        }
    }

    internal fun logDevice() {
        if (Log.canLog(LogLevel.Verbose)) {
            val dashLine = "-".repeat(20)
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
            Log.v(DEVICE, device.toString())
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
        }
    }

    internal fun logAppReleaseSDK() {
        if (Log.canLog(LogLevel.Verbose)) {
            val dashLine = "-".repeat(20)
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE START $dashLine")
            Log.v(APP_RELEASE, appRelease.toString())
            Log.v(SDK, sdk.toString())
            Log.v(CONVERSATION, "\n$dashLine CONVERSATION STATE CHANGE END $dashLine")
        }
    }

    private fun logReducedConversationObject(): String {
        return javaClass.simpleName +
            "(localIdentifier=\"$localIdentifier\", " +
            "conversationToken=\"${SensitiveDataUtils.hideIfSanitized(conversationToken)}\", " +
            "conversationID=\"$conversationId\")"
    }
}

internal val Conversation.hasConversationToken get() = this.conversationToken != null
