package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult

/**
 * Represents an object responsible for engaging events in a specific context.
 */
interface Engagement {
    fun engage(context: EngagementContext, event: Event): EngagementResult
}

/**
 * No-op engagement implementation.
 */
class NullEngagement : Engagement {
    override fun engage(context: EngagementContext, event: Event): EngagementResult {
        return EngagementResult.Failure("Unable to engage event $event: SDK is not fully initialized")
    }
}
