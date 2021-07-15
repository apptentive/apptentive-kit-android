package apptentive.com.android.feedback.link

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import apptentive.com.android.util.Log

class NavigateToLinkInteractionLauncher : AndroidInteractionLauncher<NavigateToLinkInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: NavigateToLinkInteraction
    ) {
        context.executors.main.execute {
            Log.i(INTERACTIONS, "Navigation attempt to URL/Deep Link: ${interaction.url}")
            Log.v(INTERACTIONS, "Navigate to URL/Deep Link interaction data: $interaction")
            LinkNavigator.navigate(context, interaction)
        }
    }
}
