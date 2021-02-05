package apptentive.com.android.feedback.link

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import org.junit.Test

class LinkNavigatorTest : TestCase() {
    @Test
    fun testSuccessfulNavigation() {
        testNavigation(activityLaunched = true)
    }

    @Test
    fun testFailedNavigation() {
        testNavigation(activityLaunched = false)
    }

    @Test
    fun testNavigationWithTarget() {
        testNavigation(activityLaunched = true, target = NavigateToLinkInteraction.Target.self)
    }

    private fun testNavigation(
        activityLaunched: Boolean,
        target: NavigateToLinkInteraction.Target = NavigateToLinkInteraction.Target.new
    ) {
        val context = createEngagementContext()
        val interaction = NavigateToLinkInteraction(
            id = "id",
            url = "https://example.com",
            target = target
        )
        LinkNavigator.navigate(context, interaction) {
            activityLaunched
        }

        assertResults(
            EngageArgs(
                event = Event.internal("navigate", InteractionType.NavigateToLink),
                interactionId = "id",
                data = mapOf(
                    "url" to "https://example.com",
                    "target" to target,
                    "success" to activityLaunched
                )
            )
        )
    }

    private fun createEngagementContext() = MockEngagementContext(
        onEngage = {
            addResult(it)
            EngagementResult.Failure("No runnable interactions")
        })
}