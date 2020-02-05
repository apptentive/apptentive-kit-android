package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult

class MockEngagementContext(onEngage: ((Event) -> EngagementResult)? = null) :
    EngagementContext(object : Engagement {
        override fun engage(context: EngagementContext, event: Event): EngagementResult {
            return onEngage?.invoke(event) ?: EngagementResult.Success
        }
    })