package apptentive.com.android.feedback.appstorerating

import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.util.Log


internal object StoreNavigator {
    fun navigate(
        context: AndroidEngagementContext,
        interaction: AppStoreRatingInteraction
    ) = navigate(
        context = context,
        interaction = interaction
    ) {
        context.tryStartActivity(
            appRatingIntent(
                applicationPackageName = context.androidContext.packageName,
                interaction = interaction
            )
        ) // this way we can use unit-tests
    }

    @MainThread
    @VisibleForTesting
    fun navigate(
        context: EngagementContext,
        interaction: AppStoreRatingInteraction,
        activityLauncher: () -> Boolean
    ) {
        context.executors.state.execute {
            val success = activityLauncher.invoke()

            val data = mapOf(
                KEY_URL to interaction.url,
                KEY_TARGET to interaction.storeID,
                KEY_SUCCESS to success
            )
            context.engage(
                event = Event.internal(CODE_POINT_NAVIGATE, interaction.type),
                interactionId = interaction.id,
                data = data
            )
        }
    }

    @VisibleForTesting
    fun appRatingIntent(applicationPackageName: String, interaction: AppStoreRatingInteraction): Intent {
        val URLStart = interaction.url ?: Constants.PLAY_STORE_URL
        val appStoreID = interaction.storeID ?: applicationPackageName
        val uri = Uri.parse("$URLStart$appStoreID")
        Log.i(INTERACTIONS, "Opening app store for rating with URI: \"$uri\"")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    private const val KEY_URL = "url"
    private const val KEY_TARGET = "target"
    private const val KEY_SUCCESS = "success"
    private const val CODE_POINT_NAVIGATE = "navigate"
}
