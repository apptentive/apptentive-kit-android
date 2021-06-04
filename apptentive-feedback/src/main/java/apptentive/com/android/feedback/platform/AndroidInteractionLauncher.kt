package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher

/**
 * Represents any [InteractionLauncher] which requires Android [Context] object
 */
abstract class AndroidInteractionLauncher<in T : Interaction> : InteractionLauncher<T> {
    override fun launchInteraction(context: EngagementContext, interaction: T) {
        launchInteraction(context as AndroidEngagementContext, interaction) // TODO: type checking
    }

    protected abstract fun launchInteraction(context: AndroidEngagementContext, interaction: T)
}