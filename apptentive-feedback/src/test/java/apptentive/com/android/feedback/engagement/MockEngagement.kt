package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult

class MockEngagement(private val callback: ((context: EngagementContext, event: Event) -> EngagementResult)? = null) : Engagement {
    override fun engage(context: EngagementContext, event: Event): EngagementResult {
        return callback?.invoke(context, event) ?: EngagementResult.Success
    }
}