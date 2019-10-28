package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InteractionDataProvider
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionId

class CriteriaInteractionDataProvider(
    private val interactions: Map<InteractionId, InteractionData>,
    private val invocations: InvocationRepository,
    private val state: TargetingState
) : InteractionDataProvider {
    override fun getInteraction(event: Event): InteractionData? {
        val invocations = invocations.getInvocations(event)
        if (invocations == null) {
            // FIXME: log statement
            return null
        }

        val interactionId = getInteractionId(invocations)
        if (interactionId == null) {
            // FIXME: log statement
            return null
        }

        return interactions[interactionId]
    }

    private fun getInteractionId(invocations: List<Invocation>): InteractionId? {
        // FIXME: exception handling
        for (invocation in invocations) {
            if (invocation.criteria.isMet(state)) {
                return invocation.interactionId
            }
        }
        return null
    }
}