package apptentive.com.android.feedback.link.interaction

import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.link.LinkNavigator

internal class NavigateToLinkInteractionLauncher : InteractionLauncher<NavigateToLinkInteraction> {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: NavigateToLinkInteraction,
        whereEvent: String?,
    ) {
        engagementContext.executors.main.execute {
            Log.i(INTERACTIONS, "Navigation attempt to URL/Deep Link: ${interaction.url}")
            Log.v(INTERACTIONS, "Navigate to URL/Deep Link interaction data: $interaction")
            LinkNavigator.navigate(engagementContext, engagementContext.getAppActivity(), interaction)
        }
    }
}
