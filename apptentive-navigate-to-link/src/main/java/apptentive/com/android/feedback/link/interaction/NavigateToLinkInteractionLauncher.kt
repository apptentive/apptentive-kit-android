package apptentive.com.android.feedback.link.interaction

import android.os.Bundle
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.link.LinkNavigator
import apptentive.com.android.feedback.link.view.NavigateTolinkActivity
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class NavigateToLinkInteractionLauncher : InteractionLauncher<NavigateToLinkInteraction> {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: NavigateToLinkInteraction
    ) {
        engagementContext.executors.main.execute {
            Log.i(INTERACTIONS, "Navigation attempt to URL/Deep Link: ${interaction.url}")
            Log.v(INTERACTIONS, "Navigate to URL/Deep Link interaction data: $interaction")
            LinkNavigator.navigate(engagementContext, engagementContext.getAppActivity(), interaction)
        }
    }
}
