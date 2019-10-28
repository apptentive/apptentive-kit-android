package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.*
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CriteriaInteractionRepositoryTest {

    @Test
    fun getInteraction() {
        val interactionId = "123456789"
        val expected = InteractionData(
            id = interactionId,
            type = "MyInteraction"
        )
        val interactions = createInteractions(listOf(expected))
        val targets = createFailedCriteriaTargets(interactionId)
        val repository =
            CriteriaInteractionRepository(
                interactions,
                targets,
                FailureTargetingState
            )

        val actual = repository.getInteraction(Event.local("event"))
        assertThat(actual).isEqualTo(expected)
    }

    private fun createInteractions(interactionList: List<InteractionData>): Map<String, InteractionData> {
        return interactionList.map {
            it.id to it
        }.toMap()
    }

    private fun createFailedCriteriaTargets(interactionId: String): InvocationRepository {
        return object : InvocationRepository {
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

