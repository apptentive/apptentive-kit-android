package apptentive.com.android.feedback.appstorerating

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.platform.tryStartActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal object StoreNavigator {
    @MainThread
    fun navigate(
        engagementContext: EngagementContext,
        activityContext: Context,
        interaction: AppStoreRatingInteraction
    ) = navigate(
        context = engagementContext,
        interaction = interaction
    ) {
        activityContext.tryStartActivity(
            appRatingIntent(
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
        val success = activityLauncher.invoke()
        if (success) Log.i(INTERACTIONS, "Store intent launch successful")
        else Log.w(INTERACTIONS, "Store intent launch un-successful")

        context.executors.state.execute {
            context.engage(Event.internal(OPEN_APP_STORE_URL, interaction.type))
        }
    }

    @VisibleForTesting
    fun appRatingIntent(interaction: AppStoreRatingInteraction): Intent {
        val uri = Uri.parse(interaction.url ?: interaction.customStoreURL)
        Log.i(INTERACTIONS, "Opening app store for rating with URI: \"$uri\"")
        return Intent(Intent.ACTION_VIEW, uri)
    }

    private const val OPEN_APP_STORE_URL = "open_app_store_url"
}
