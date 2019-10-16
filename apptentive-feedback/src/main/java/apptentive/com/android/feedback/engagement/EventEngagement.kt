package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult

// TODO: a better name
interface EventEngagement {
    fun engage(context: EngagementContext, event: Event): EngagementResult
}

class NullEventEngagement : EventEngagement {
    override fun engage(context: EngagementContext, event: Event): EngagementResult {
        return EngagementResult.Failure("Unable to engage event $event: SDK is not fully initialized")
    }
}
