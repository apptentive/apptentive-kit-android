package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.InteractionFactory

@Suppress("FoldInitializerAndIfToElvis")
class DefaultEventEngagement(
    private val interactionResolver: InteractionResolver,
    private val interactionFactory: InteractionFactory,
    private val interactionEngagement: InteractionEngagement
) : EventEngagement {
    override fun engage(event: Event): EngagementResult {
        val interactionData = interactionResolver.getInteraction(event)
        if (interactionData == null) {
            return EngagementResult.Failure("No runnable interactions for event '${event.name}'")
        }

        val interaction = interactionFactory.createInteraction(interactionData)
        if (interaction == null) {
            return EngagementResult.Failure("Unknown interaction type '${interactionData.type}' for event '${event.name}'")
        }

        return interactionEngagement.engage(interaction)
    }
}
