package apptentive.com.android.feedback.appstorerating

import android.content.Context
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class AppStoreRatingInteractionLauncher : InteractionLauncher<AppStoreRatingInteraction> {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: AppStoreRatingInteraction
    ) {

        val sharedPrefs = engagementContext.getAppActivity().getSharedPreferences(SharedPrefConstants.CUSTOM_STORE_URL, Context.MODE_PRIVATE)
        val customAppStoreURL = sharedPrefs.getString(SharedPrefConstants.CUSTOM_STORE_URL_KEY, null)
        interaction.customStoreURL = customAppStoreURL

        Log.i(INTERACTIONS, "App Store Rating navigate attempt to: ${interaction.url ?: interaction.customStoreURL}")

        StoreNavigator.navigate(engagementContext, engagementContext.getAppActivity(), interaction)
    }
}
