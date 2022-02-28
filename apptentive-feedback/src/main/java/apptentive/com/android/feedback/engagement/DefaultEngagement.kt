package apptentive.com.android.feedback.engagement

import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.EVENT
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionDataConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.util.Log

internal typealias RecordEventCallback = (
    event: Event,
    interactionId: String?,
    data: Map<String, Any?>?,
    customData: Map<String, Any?>?,
    extendedData: List<ExtendedData>?
) -> Unit

internal typealias RecordInteractionCallback = (interaction: Interaction) -> Unit

internal typealias RecordInteractionResponsesCallback = (Map<String, Set<InteractionResponse>>) -> Unit

@Suppress("FoldInitializerAndIfToElvis")
internal data class DefaultEngagement(
    private val interactionDataProvider: InteractionDataProvider,
    private val interactionConverter: InteractionDataConverter,
    private val interactionEngagement: InteractionEngagement,
    private val recordEvent: RecordEventCallback,
    private val recordInteraction: RecordInteractionCallback,
    private val recordInteractionResponses: RecordInteractionResponsesCallback
) : Engagement {
    @WorkerThread
    override fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String?,
        data: Map<String, Any?>?,
        customData: Map<String, Any?>?,
        extendedData: List<ExtendedData>?,
        interactionResponses: Map<String, Set<InteractionResponse>>?
    ): EngagementResult {
        Log.i(EVENT, "Engaged event: $event")
        Log.d(EVENT, "Engaged event interaction ID: $interactionId")
        recordEvent(event, interactionId, data, customData, extendedData)

        if (interactionResponses != null) recordInteractionResponses(interactionResponses)

        val interactionData = interactionDataProvider.getInteractionData(event)
        if (interactionData == null) {
            return EngagementResult.InteractionNotShown("No runnable interactions for event '${event.name}'")
        }

        val interaction = interactionConverter.convert(interactionData)
        if (interaction == null) {
            return EngagementResult.Error("Unknown interaction type '${interactionData.type}' for event '${event.name}'")
        }

        return engage(context, interaction)
    }

    override fun engage(
        context: EngagementContext,
        invocations: List<Invocation>
    ): EngagementResult {
        val interactionData = interactionDataProvider.getInteractionData(invocations)
        if (interactionData == null) {
            return EngagementResult.InteractionNotShown("No runnable interactions")
        }

        val interaction = interactionConverter.convert(interactionData)
        if (interaction == null) {
            return EngagementResult.Error("Unknown interaction type '${interactionData.type}'")
        }

        return engage(context, interaction)
    }

    private fun engage(
        context: EngagementContext,
        interaction: Interaction
    ): EngagementResult {
        val result = interactionEngagement.engage(context, interaction)
        if (result is EngagementResult.InteractionShown) {
            recordInteraction(interaction)
        }

        return result
    }
}
