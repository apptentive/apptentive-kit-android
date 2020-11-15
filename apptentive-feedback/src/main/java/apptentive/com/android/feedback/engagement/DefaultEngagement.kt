package apptentive.com.android.feedback.engagement

import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionDataConverter
import apptentive.com.android.feedback.model.payloads.ExtendedData

typealias RecordEventCallback = (
    event: Event,
    interactionId: String?,
    data: Map<String, Any>?,
    customData: Map<String, Any>?,
    extendedData: List<ExtendedData>?
) -> Unit

typealias RecordInteractionCallback = (interaction: Interaction) -> Unit

@Suppress("FoldInitializerAndIfToElvis")
data class DefaultEngagement(
    private val interactionDataProvider: InteractionDataProvider,
    private val interactionConverter: InteractionDataConverter,
    private val interactionEngagement: InteractionEngagement,
    private val recordEvent: RecordEventCallback,
    private val recordInteraction: RecordInteractionCallback
) : Engagement {
    @WorkerThread
    override fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String?,
        data: Map<String, Any>?,
        customData: Map<String, Any>?,
        extendedData: List<ExtendedData>?
    ): EngagementResult {
        recordEvent(event, interactionId, data, customData, extendedData)

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
