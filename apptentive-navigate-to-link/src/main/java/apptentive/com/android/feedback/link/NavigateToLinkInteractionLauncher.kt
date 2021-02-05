package apptentive.com.android.feedback.link

import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher

class NavigateToLinkInteractionLauncher : AndroidInteractionLauncher<NavigateToLinkInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: NavigateToLinkInteraction
    ) {
        context.executors.main.execute {
            LinkNavigator.navigate(context, interaction)
        }
    }
}
