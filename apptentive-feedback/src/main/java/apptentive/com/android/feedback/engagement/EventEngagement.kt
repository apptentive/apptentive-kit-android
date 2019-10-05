package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult

interface EventEngagement {
    fun engage(event: Event): EngagementResult
}
