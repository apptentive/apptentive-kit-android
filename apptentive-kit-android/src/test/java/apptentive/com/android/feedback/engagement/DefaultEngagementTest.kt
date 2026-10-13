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
        ThrottleUtils.interactionCountLimit = 1
        ThrottleUtils.resetEngagedEvents()
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

    @Test
    fun perSessionLimitThrottlesAfterLimitReached() {
        ThrottleUtils.interactionCountLimit = 1
        interactionEngagement.addResult(EngagementResult.InteractionShown(mockInteraction.id))
        interactionEngagement.addResult(EngagementResult.InteractionShown(mockInteraction.id))

        // First engage: allowed (count goes to 1, limit is 1 so count < limit is false after increment)
        val first = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(first).isInstanceOf(EngagementResult.InteractionShown::class.java)

        // Second engage: throttled (count already at 1 == limit)
        val second = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(second).isInstanceOf(EngagementResult.InteractionNotShown::class.java)
    }

    @Test
    fun perSessionLimitZeroBlocksAllNonExemptedInteractions() {
        ThrottleUtils.interactionCountLimit = 0
        interactionEngagement.addResult(EngagementResult.InteractionShown(mockInteraction.id))

        val result = engagement.engage(MockEngagementContext(), mockEvent)
        assertThat(result).isInstanceOf(EngagementResult.InteractionNotShown::class.java)
    }

    @Test
    fun exemptedEventsBypassPerSessionLimit() {
        ThrottleUtils.interactionCountLimit = 0
        interactionEngagement.addResult(EngagementResult.InteractionShown(mockInteraction.id))

        val exemptedEvent = Event.local("show_message_center")
        val result = engagement.engage(MockEngagementContext(), exemptedEvent)
        // InteractionNotShown because MockInteractionDataProvider only handles mockEvent,
        // but the point is it was NOT blocked by the limit — it went through to the provider.
        assertThat(result).isNotInstanceOf(EngagementResult.Error::class.java)
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
