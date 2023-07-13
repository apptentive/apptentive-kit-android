package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.SURVEY

/**
 * Represents an object responsible for engaging events in a specific context.
 */
@InternalUseOnly
interface Engagement {
    fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String? = null,
        data: Map<String, Any?>? = null,
        customData: Map<String, Any?>? = null,
        extendedData: List<ExtendedData>? = null,
        interactionResponses: Map<String, Set<InteractionResponse>>? = null
    ): EngagementResult

    fun engage(context: EngagementContext, invocations: List<Invocation>): EngagementResult

    fun engageToRecordCurrentAnswer(interactionResponses: Map<String, Set<InteractionResponse>>, reset: Boolean)

    fun getNextQuestionSet(invocations: List<Invocation>): String?
}

/**
 * No-op engagement implementation.
 */
internal class NullEngagement : Engagement {
    override fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String?,
        data: Map<String, Any?>?,
        customData: Map<String, Any?>?,
        extendedData: List<ExtendedData>?,
        interactionResponses: Map<String, Set<InteractionResponse>>?
    ): EngagementResult {
        return EngagementResult.Error("Unable to engage event $event: SDK is not fully initialized")
    }

    override fun engage(
        context: EngagementContext,
        invocations: List<Invocation>
    ): EngagementResult {
        return EngagementResult.Error("Unable to engage invocations: SDK is not fully initialized")
    }

    override fun engageToRecordCurrentAnswer(
        interactionResponses: Map<String, Set<InteractionResponse>>,
        reset: Boolean
    ) {
        Log.e(SURVEY, "Unable to record current answer: SDK is not fully initialized")
    }

    override fun getNextQuestionSet(
        invocations: List<Invocation>
    ): String? {
        Log.e(SURVEY, "Unable to get next quest set: SDK is not fully initialized")
        return null
    }
}
