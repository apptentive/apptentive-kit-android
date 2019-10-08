package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult

// TODO: a better name
interface EventEngagement {
    fun engage(event: Event): EngagementResult
}
