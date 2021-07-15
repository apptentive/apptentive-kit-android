package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher

/**
 * Represents any [InteractionLauncher] which requires Android [Context] object and has user interface.
 * It would automatically engage "launch" event when the interaction is launched.
 */
abstract class AndroidViewInteractionLauncher<in T : Interaction> :
    AndroidInteractionLauncher<T>() {
    override fun launchInteraction(context: EngagementContext, interaction: T) {
        // every interaction which has a user interface must engage internal "launch" event before showing UI
        context.engage(Event.internal("launch", interaction.type), interaction.id)

        // create and display UI
        super.launchInteraction(context, interaction)
    }
}
