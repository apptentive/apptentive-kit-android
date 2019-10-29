package apptentive.com.android.feedback.engagement

/**
 * Wrapper class around [Engagement] object.
 * Allows capturing platform specific context (e.g. [android.content.Context]) before making an
 * actual engagement call.
 */
abstract class EngagementContext(private val engagement: Engagement) {
    fun engage(event: Event) = engagement.engage(this, event)
}