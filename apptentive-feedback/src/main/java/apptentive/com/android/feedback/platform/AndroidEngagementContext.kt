package apptentive.com.android.feedback.platform

import android.content.Context
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.Engagement

class AndroidEngagementContext(
    val androidContext: Context,
    private val engagement: Engagement
) : EngagementContext {
    override fun engage(event: Event) {
        engagement.engage(this, event)
    }
}