package apptentive.com.android.feedback.engagement

class MockEngagementContext(private val onEngage: (Event) -> Unit = {}) : EngagementContext {
    override fun engage(event: Event) {
        onEngage(event)
    }
}