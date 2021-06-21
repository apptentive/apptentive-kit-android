package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.engagement.EngagementContext

interface InteractionLauncher<in T : Interaction> {
    fun launchInteraction(context: EngagementContext, interaction: T)
}
