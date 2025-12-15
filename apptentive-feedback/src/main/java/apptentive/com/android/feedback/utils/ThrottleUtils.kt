package apptentive.com.android.feedback.utils

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.LogTags.INTERACTIONS

internal object ThrottleUtils {
    internal var ratingThrottleLength: Long? = null
    internal const val CONVERSATION_TYPE = "Conversation"
    internal const val ROSTER_TYPE = "Roster"
    internal var exemptedEvents = setOf("show_message_center", "message_center_fallback", "rating_dialog_event", "app_review_event", "EnjoymentDialog#no", "EnjoymentDialog#yes")
    internal var interactionCountLimit = 1
    internal val engagedInteractions = mutableMapOf<String, Int>()

    fun shouldThrottleInteraction(eventName: String, interaction: Interaction): Boolean {
        val interactionType = interaction.type

        return when {
            interactionType == InteractionType.Initiator -> false
            interactionCountLimit <= 0 -> true
            interaction.type == InteractionType.RatingDialog || interaction.type == InteractionType.GoogleInAppReview -> {
                val currentTime = System.currentTimeMillis()
                val lastThrottledTime = DependencyProvider.of<AndroidSharedPrefDataStore>().getLong(SharedPrefConstants.THROTTLE_UTILS, interaction.type.name, 0)
                val elapsedTimeSinceLastInteraction = currentTime - lastThrottledTime
                return ratingThrottleLength?.let { ratingLength ->
                    when {
                        elapsedTimeSinceLastInteraction < ratingLength -> {
                            logThrottle(interaction, ratingLength, elapsedTimeSinceLastInteraction)
                            true
                        }
                        else -> {
                            DependencyProvider.of<AndroidSharedPrefDataStore>().putLong(SharedPrefConstants.THROTTLE_UTILS, interaction.type.name, currentTime)
                            false
                        }
                    }
                } ?: false
            }
            eventName in exemptedEvents -> false
            interaction.id in engagedInteractions -> {
                val count = engagedInteractions[interaction.id] ?: 0
                if (count < interactionCountLimit) {
                    engagedInteractions[interaction.id] = count + 1
                    false
                } else {
                    false // Not throttling for 6.10
                }
            }
            else -> {
                engagedInteractions[interaction.id] = 1
                false
            }
        }
    }

    fun resetEngagedEvents() {
        engagedInteractions.clear()
    }

    fun shouldThrottleReset(fileType: String): Boolean {
        val sharedPrefDataStore = DependencyProvider.of<AndroidSharedPrefDataStore>()
        val sdkVersion = if (fileType == CONVERSATION_TYPE) {
            sharedPrefDataStore.getString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.CONVERSATION_RESET_THROTTLE
            )
        } else {
            sharedPrefDataStore.getString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.ROSTER_RESET_THROTTLE
            )
        }
        val apptentiveSDKVersion: String = Constants.SDK_VERSION
        return if (sdkVersion.isEmpty() || sdkVersion != apptentiveSDKVersion) {
            Log.d(CONVERSATION, "$fileType reset NOT throttled")
            sharedPrefDataStore.putString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.CONVERSATION_RESET_THROTTLE,
                Constants.SDK_VERSION
            )
            false
        } else {
            Log.d(CONVERSATION, "$fileType reset throttled")
            true
        }
    }

    fun shouldThrottleReset(fileType: String): Boolean {
        val sharedPrefDataStore = DependencyProvider.of<AndroidSharedPrefDataStore>()
        val sdkVersion = if (fileType == CONVERSATION_TYPE) {
            sharedPrefDataStore.getString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.CONVERSATION_RESET_THROTTLE
            )
        } else {
            sharedPrefDataStore.getString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.ROSTER_RESET_THROTTLE
            )
        }
        val apptentiveSDKVersion: String = Constants.SDK_VERSION
        return if (sdkVersion.isEmpty() || sdkVersion != apptentiveSDKVersion) {
            Log.d(CONVERSATION, "$fileType reset NOT throttled")
            sharedPrefDataStore.putString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.CONVERSATION_RESET_THROTTLE,
                Constants.SDK_VERSION
            )
            false
        } else {
            Log.d(CONVERSATION, "$fileType reset throttled")
            true
        }
    }

    private fun logThrottle(
        interaction: Interaction,
        throttleLength: Long,
        interactionLastThrottledLength: Long
    ) {
        val interactionName = interaction.type.name
        val interactionID = interaction.id

        Log.w(
            INTERACTIONS,
            "$interactionName with id $interactionID throttled. " +
                "Throttle length is ${throttleLength}ms. " +
                "Can be shown again in ${throttleLength - interactionLastThrottledLength}ms."
        )
    }
}
