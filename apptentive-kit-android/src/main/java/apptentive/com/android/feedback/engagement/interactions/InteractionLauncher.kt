package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.engagement.EngagementContext

internal interface InteractionLauncher<in T : Interaction> {
    fun launchInteraction(engagementContext: EngagementContext, interaction: T, whereEvent: String? = null)
}
