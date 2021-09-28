package apptentive.com.android.feedback.appstorerating

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.util.Log

internal class AppStoreRatingInteractionLauncher : AndroidViewInteractionLauncher<AppStoreRatingInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: AppStoreRatingInteraction
    ) {
        Log.i(INTERACTIONS, "App Store Rating navigate attempt to store package: ${context.androidContext.packageName}")

        StoreNavigator.navigate(context, interaction)
    }
}
