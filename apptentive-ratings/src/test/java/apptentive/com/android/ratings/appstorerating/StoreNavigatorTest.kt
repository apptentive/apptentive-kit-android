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

    private val interactionWithProvidedURL = AppStoreRatingInteraction(
        id = "id",
        storeID = null,
        method = null,
        url = "https://example.com",
        customStoreURL = null
    )

    private val interactionWithCustomURL = AppStoreRatingInteraction(
        id = "id",
        storeID = null,
        method = null,
        url = null,
        customStoreURL = "https://custom-example.com"
    )

    @Test
    fun testSuccessfulProvidedNavigation() {
        testNavigation(interaction = interactionWithProvidedURL, activityLaunched = true)
    }

    @Test
    fun testFailedProvidedNavigation() {
        testNavigation(interaction = interactionWithProvidedURL, activityLaunched = false)
    }

    @Test
    fun testProvidedNavigationWithTarget() {
        testNavigation(interaction = interactionWithProvidedURL, activityLaunched = true)
    }

    @Test
    fun testSuccessfulCustomNavigation() {
        testNavigation(interaction = interactionWithCustomURL, activityLaunched = true)
    }

    @Test
    fun testFailedCustomNavigation() {
        testNavigation(interaction = interactionWithCustomURL, activityLaunched = false)
    }

    @Test
    fun testCustomNavigationWithTarget() {
        testNavigation(interaction = interactionWithCustomURL, activityLaunched = true)
    }

    private fun testNavigation(interaction: AppStoreRatingInteraction, activityLaunched: Boolean) {
        val context = createEngagementContext()
        StoreNavigator.navigate(context, interaction) {
            activityLaunched
        }

        assertResults(
            EngageArgs(
                event = Event.internal("open_app_store_url", InteractionType.AppStoreRating)
            )
        )
    }

    private fun createEngagementContext() = MockEngagementContext(
        onEngage = {
            addResult(it)
            EngagementResult.Failure("No runnable interactions")
        })
}