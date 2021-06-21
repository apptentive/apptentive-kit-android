package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.CriteriaInteractionDataProvider
import apptentive.com.android.feedback.engagement.criteria.Field
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.criteria.InvocationProvider
import apptentive.com.android.feedback.engagement.criteria.TargetingState
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

class CriteriaInteractionDataProviderTest {
    @Test
    @Ignore
    fun getInteraction() {
        val interactionId = "123456789"
        val expected = InteractionData(
            id = interactionId,
            type = "MyInteraction"
        )
        val interactions = createInteractions(listOf(expected))
        val invocationProvider = createFailedInvocationProvider(interactionId)
        val provider = CriteriaInteractionDataProvider(
            interactions = interactions,
            invocationProvider = invocationProvider,
            state = FailureTargetingState
        )

        val actual = provider.getInteractionData(Event.local("event"))
        assertThat(actual).isEqualTo(expected)
    }

    private fun createInteractions(interactionList: List<InteractionData>): Map<String, InteractionData> {
        return interactionList.map {
            it.id to it
        }.toMap()
    }

    private fun createFailedInvocationProvider(interactionId: String): InvocationProvider {
        return object : InvocationProvider {
            override fun getInvocations(event: Event): List<Invocation>? {
                return listOf(
                    Invocation(
                        interactionId = interactionId,
                        criteria = FailureInteractionCriteria
                    )
                )
            }
        }
    }
}

private object FailureTargetingState : TargetingState {
    override fun getValue(field: Field): Any? {
        throw AssertionError("Should never get here")
    }
}
