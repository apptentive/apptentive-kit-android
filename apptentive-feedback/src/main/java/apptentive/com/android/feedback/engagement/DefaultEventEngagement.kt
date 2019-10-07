package apptentive.com.android.feedback.engagement

import android.content.Context
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionFactory

@Suppress("FoldInitializerAndIfToElvis")
class DefaultEventEngagement(
    private val interactionResolver: InteractionResolver,
    private val interactionFactory: InteractionFactory,
    private val interactionEngagement: InteractionEngagement,
    private val recordEvent: (Event) -> Unit = {},
    private val recordInteraction: (Interaction) -> Unit = {}
) : EventEngagement {
    override fun engage(context: Context, event: Event): EngagementResult {
        recordEvent(event)

        val interactionData = interactionResolver.getInteraction(event)
        if (interactionData == null) {
            return EngagementResult.Failure("No runnable interactions for event '${event.name}'")
        }

        val interaction = interactionFactory.createInteraction(interactionData)
        if (interaction == null) {
            return EngagementResult.Error("Unknown interaction type '${interactionData.type}' for event '${event.name}'") // TODO: more description error message
        }

        val result = interactionEngagement.engage(context, interaction)
        if (result is EngagementResult.Success) {
            recordInteraction(interaction)
        }

        return result
    }
}
