package apptentive.com.android.feedback.engagement

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.engagement.interactions.MockInteractionDataConverter
import apptentive.com.android.feedback.engagement.interactions.mockEvent
import apptentive.com.android.feedback.engagement.interactions.mockInteraction
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.utils.ThrottleUtils
import com.google.common.truth.Truth.assertThat
import org.junit.After
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
            recordInteraction = ::recordInteraction,
            recordInteractionResponses = ::recordInteractionResponses,
            recordCurrentAnswer = ::recordCurrentAnswer
        )
    }

    @After
    fun cleanup() {
        ThrottleUtils.sdkEnabled = true
    }

    @Test
    fun engageWhenSdkDisabled() {
        ThrottleUtils.sdkEnabled = false

        val result = engagement.engage(MockEngagementContext(), mockEvent)

        assertThat(result).isInstanceOf(EngagementResult.InteractionNotShown::class.java)
        assertResults()
    }

    @Test
    fun engageSuccessful() {
        interactionEngagement.addResult(EngagementResult.InteractionShown(mockInteraction.id))

        val result = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(result).isInstanceOf(EngagementResult.InteractionShown::class.java)

        assertResults(
            "Event: ${mockEvent.fullName}",
            "Interaction: ${mockInteraction.id}"
        )
    }

    @Test
    fun engageUnsuccessful() {
        interactionEngagement.addResult(EngagementResult.InteractionNotShown("Something went wrong"))

        val result = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(result).isInstanceOf(EngagementResult.InteractionNotShown::class.java)

        assertResults("Event: ${mockEvent.fullName}")
    }

    private fun recordEvent(
        event: Event,
        interactionId: String?,
        data: Map<String, Any?>?,
        customData: Map<String, Any?>?,
        extendedData: List<ExtendedData>?,
        whereEvent: String?,
    ) {
        addResult("Event: ${event.fullName}")
    }

    private fun recordInteraction(interaction: Interaction) {
        addResult("Interaction: ${interaction.id}")
    }

    private fun recordInteractionResponses(interactionResponses: Map<String, Set<InteractionResponse>>) {
        addResult("Interaction Responses: $interactionResponses")
    }

    private fun recordCurrentAnswer(interactionResponses: Map<String, Set<InteractionResponse>>, reset: Boolean) {
    }
}
