package apptentive.com.android.feedback.engagement

import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionDataConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.EVENT

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
            return EngagementResult.InteractionNotShown("No invocations found or criteria evaluated false for event: '${event.name}'")
        }

        val interaction = interactionConverter.convert(interactionData)
        if (interaction == null) {
            // Cannot find module to handle interaction
            return EngagementResult.Error("Cannot find '${interactionData.type}' module to handle event '${event.name}'")
        }

        return engage(context, interaction)
    }

    // This engage is only used for Note actions
    override fun engage(
        context: EngagementContext,
        invocations: List<Invocation>
    ): EngagementResult {
        val interactionData = interactionDataProvider.getInteractionData(invocations)
        if (interactionData == null) {
            // Cannot find interaction to handle Note action invocation in manifest.
            return EngagementResult.Error("Interaction to handle $invocations NOT found")
        }

        val interaction = interactionConverter.convert(interactionData)
        if (interaction == null) {
            // Cannot find module to handle interaction for Note action
            return EngagementResult.Error("Cannot find $interaction module to handle '$interactionData'")
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
