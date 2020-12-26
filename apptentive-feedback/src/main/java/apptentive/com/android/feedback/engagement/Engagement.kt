package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.model.payloads.ExtendedData

/**
 * Represents an object responsible for engaging events in a specific context.
 */
interface Engagement {
    fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String? = null,
        data: Map<String, Any>? = null,
        customData: Map<String, Any>? = null,
        extendedData: List<ExtendedData>? = null
    ): EngagementResult

    fun engage(context: EngagementContext, invocations: List<Invocation>): EngagementResult
}

/**
 * No-op engagement implementation.
 */
class NullEngagement : Engagement {
    override fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String?,
        data: Map<String, Any>?,
        customData: Map<String, Any>?,
        extendedData: List<ExtendedData>?
    ): EngagementResult {
        return EngagementResult.Failure("Unable to engage event $event: SDK is not fully initialized")
    }

    override fun engage(
        context: EngagementContext,
        invocations: List<Invocation>
    ): EngagementResult {
        return EngagementResult.Failure("Unable to engage invocations: SDK is not fully initialized")
    }
}
