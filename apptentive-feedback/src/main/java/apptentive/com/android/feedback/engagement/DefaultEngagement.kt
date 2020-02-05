package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionDataConverter

@Suppress("FoldInitializerAndIfToElvis")
data class DefaultEngagement(
    private val interactionDataProvider: InteractionDataProvider,
    private val interactionConverter: InteractionDataConverter,
    private val interactionEngagement: InteractionEngagement,
    private val recordEvent: (Event) -> Unit = {},
    private val recordInteraction: (Interaction) -> Unit = {}
) : Engagement {
    override fun engage(context: EngagementContext, event: Event): EngagementResult {
        recordEvent(event)

        val interactionData = interactionDataProvider.getInteraction(event)
        if (interactionData == null) {
            return EngagementResult.Failure("No runnable interactions for event '${event.name}'")
        }

        val interaction = interactionConverter.convert(interactionData)
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