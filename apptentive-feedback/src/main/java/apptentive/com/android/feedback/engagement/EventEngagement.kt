package apptentive.com.android.feedback.engagement

import android.content.Context
import apptentive.com.android.feedback.EngagementResult

// TODO: a better name
interface EventEngagement {
    fun engage(context: Context, event: Event): EngagementResult
}
