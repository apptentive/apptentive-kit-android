package apptentive.com.android.ratings.appstorerating

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.appstorerating.AppStoreRatingInteraction
import apptentive.com.android.feedback.appstorerating.StoreNavigator
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import org.junit.Test

class StoreNavigatorTest : TestCase() {

    private val interaction = AppStoreRatingInteraction(
        id = "id",
        storeID = "com.apptentive",
        method = "amazon",
        url = "https://example.com",
    )

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
        testNavigation(activityLaunched = true)
    }

    private fun testNavigation(activityLaunched: Boolean, ) {
        val context = createEngagementContext()
        StoreNavigator.navigate(context, interaction) {
            activityLaunched
        }

        assertResults(
            EngageArgs(
                event = Event.internal("navigate", InteractionType.AppStoreRating),
                interactionId = "id",
                data = mapOf(
                    "url" to  interaction.url.orEmpty(),
                    "target" to interaction.storeID.orEmpty(),
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