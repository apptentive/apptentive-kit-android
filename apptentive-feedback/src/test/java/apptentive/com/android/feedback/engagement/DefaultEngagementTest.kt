package apptentive.com.android.feedback.engagement

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.MockInteractionDataConverter
import apptentive.com.android.feedback.engagement.interactions.mockEvent
import apptentive.com.android.feedback.engagement.interactions.mockInteraction
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class DefaultEngagementTest : TestCase() {
    private lateinit var engagement: Engagement
    private lateinit var interactionEngagement: MockInteractionEngagement

    @Before
    fun setup() {
        interactionEngagement = MockInteractionEngagement()
        engagement = DefaultEngagement(
            interactionDataProvider = MockInteractionDataProvider(),
            interactionConverter = MockInteractionDataConverter(),
            interactionEngagement = interactionEngagement,
            recordEvent = ::recordEvent,
            recordInteraction = ::recordInteraction
        )
    }

    @Test
    fun engageSuccessful() {
        interactionEngagement.addResult(EngagementResult.Success)

        val result = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(result).isEqualTo(EngagementResult.Success)

        assertResults(
            "Event: ${mockEvent.fullName}",
            "Interaction: ${mockInteraction.id}"
        )
    }

    @Test
    fun engageUnsuccessful() {
        interactionEngagement.addResult(EngagementResult.Failure("Something went wrong"))

        val result = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(result).isInstanceOf(EngagementResult.Failure::class.java)

        assertResults("Event: ${mockEvent.fullName}")
    }

    private fun recordEvent(event: Event) {
        addResult("Event: ${event.fullName}")
    }

    private fun recordInteraction(interaction: Interaction) {
        addResult("Interaction: ${interaction.id}")
    }
}