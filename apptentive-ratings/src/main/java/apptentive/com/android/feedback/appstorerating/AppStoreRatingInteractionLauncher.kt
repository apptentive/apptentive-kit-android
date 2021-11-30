package apptentive.com.android.feedback.appstorerating

import android.content.Context
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import apptentive.com.android.util.Log

internal class AppStoreRatingInteractionLauncher : AndroidInteractionLauncher<AppStoreRatingInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: AppStoreRatingInteraction
    ) {

        val sharedPrefs = context.androidContext.getSharedPreferences(Constants.SHARED_PREF_CUSTOM_STORE_URL, Context.MODE_PRIVATE)
        val customAppStoreURL = sharedPrefs.getString(Constants.SHARED_PREF_CUSTOM_STORE_URL_KEY, null)
        interaction.customStoreURL = customAppStoreURL

        Log.i(INTERACTIONS, "App Store Rating navigate attempt to: ${interaction.url ?: interaction.customStoreURL}")

        StoreNavigator.navigate(context, interaction)
    }
}
