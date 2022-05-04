package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.util.InternalUseOnly

/**
 * Represents any [InteractionLauncher] which requires Android [Context] object and has user interface.
 * It would automatically engage "launch" event when the interaction is launched.
 */
@InternalUseOnly
abstract class AndroidViewInteractionLauncher<in T : Interaction> : InteractionLauncher<T> {
    override fun launchInteraction(engagementContext: EngagementContext, interaction: T) {
        // every interaction which has a user interface must engage internal "launch" event before showing UI
        engagementContext.engage(
            Event.internal(InternalEvent.EVENT_LAUNCH.labelName, interaction.type),
            interaction.id
        )
    }
}
