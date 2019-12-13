package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors

/**
 * Wrapper class around [Engagement] object.
 * Allows capturing platform specific context (e.g. [android.content.Context]) before making an
 * actual engagement call.
 */
open class EngagementContext(
    private val engagement: Engagement,
    val executors: Executors
) {
    fun engage(event: Event) = engagement.engage(this, event)
}