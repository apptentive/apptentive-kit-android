package apptentive.com.android.feedback.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.LogTags.INTERACTIONS
import java.util.concurrent.TimeUnit

internal object ThrottleUtils {
    internal var ratingThrottleLength: Long? = null
    internal var throttleSharedPrefs: SharedPreferences? = null

    private val defaultThrottleLength = TimeUnit.SECONDS.toMillis(1)

    @SuppressLint("ApplySharedPref")
    fun shouldThrottleInteraction(interaction: Interaction): Boolean {
        val interactionName = interaction.type.name
        val interactionIsRating = interaction.type in
            listOf(InteractionType.GoogleInAppReview, InteractionType.RatingDialog)

        val currentTime = System.currentTimeMillis()
        val interactionLastThrottledTime = throttleSharedPrefs?.getLong(interactionName, 0) ?: 0
        val interactionLastThrottledLength = currentTime - interactionLastThrottledTime

        return ratingThrottleLength?.let { ratingLength ->
            when {
                interactionIsRating && interactionLastThrottledLength < ratingLength -> {
                    logThrottle(interaction, ratingLength, interactionLastThrottledLength)
                    true
                }
                !interactionIsRating && interactionLastThrottledLength < defaultThrottleLength -> {
                    logThrottle(interaction, defaultThrottleLength, interactionLastThrottledLength)
                    true
                }
                else -> {
                    throttleSharedPrefs?.edit()?.putLong(interactionName, currentTime)?.commit()
                    false
                }
            }
        } ?: false
    }

    fun shouldThrottleResetConversation(): Boolean {
        val sharedPrefDataStore = DependencyProvider.of<AndroidSharedPrefDataStore>()
        val sdkVersion: String = sharedPrefDataStore.getString(
            SharedPrefConstants.THROTTLE_UTILS,
            SharedPrefConstants.CONVERSATION_RESET_THROTTLE
        )
        val apptentiveSDKVersion: String = Constants.SDK_VERSION

        return if (sdkVersion.isEmpty() || sdkVersion != apptentiveSDKVersion) {
            Log.d(CONVERSATION, "Conversation reset NOT throttled")
            sharedPrefDataStore.putString(
                SharedPrefConstants.THROTTLE_UTILS,
                SharedPrefConstants.CONVERSATION_RESET_THROTTLE,
                Constants.SDK_VERSION
            )
            false
        } else {
            Log.d(CONVERSATION, "Conversation reset throttled")
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
