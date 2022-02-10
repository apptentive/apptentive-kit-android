package apptentive.com.android.feedback.appstorerating

import android.content.Context
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.util.Log

internal class AppStoreRatingInteractionLauncher : InteractionLauncher<AppStoreRatingInteraction> {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: AppStoreRatingInteraction
    ) {

        val sharedPrefs = engagementContext.getActivityContext().getSharedPreferences(Constants.SHARED_PREF_CUSTOM_STORE_URL, Context.MODE_PRIVATE)
        val customAppStoreURL = sharedPrefs.getString(Constants.SHARED_PREF_CUSTOM_STORE_URL_KEY, null)
        interaction.customStoreURL = customAppStoreURL

        Log.i(INTERACTIONS, "App Store Rating navigate attempt to: ${interaction.url ?: interaction.customStoreURL}")

        StoreNavigator.navigate(engagementContext, engagementContext.getActivityContext(), interaction)
    }
}
