package apptentive.com.android.feedback.appstorerating

import apptentive.com.android.core.platform.SharedPrefConstants
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState

internal class AppStoreRatingInteractionLauncher : InteractionLauncher<AppStoreRatingInteraction> {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: AppStoreRatingInteraction,
        whereEvent: String?,
    ) {
        val customAppStoreURL = ApptentiveKitSDKState.getSharedPrefDataStore()
            .getNullableString(SharedPrefConstants.CUSTOM_STORE_URL, SharedPrefConstants.CUSTOM_STORE_URL_KEY, null)
        interaction.customStoreURL = customAppStoreURL

        Log.i(INTERACTIONS, "App Store Rating navigate attempt to: ${interaction.url ?: interaction.customStoreURL}")

        StoreNavigator.navigate(engagementContext, engagementContext.getAppActivity(), interaction)
    }
}
