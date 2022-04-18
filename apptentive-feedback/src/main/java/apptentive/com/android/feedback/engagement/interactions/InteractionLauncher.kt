package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface InteractionLauncher<in T : Interaction> {
    fun launchInteraction(engagementContext: EngagementContext, interaction: T)
}
