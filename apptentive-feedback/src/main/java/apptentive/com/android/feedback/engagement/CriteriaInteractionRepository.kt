package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.criteria.TargetRepository
import apptentive.com.android.feedback.engagement.criteria.TargetingState
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionId

class CriteriaInteractionRepository(
    private val interactions: Map<InteractionId, InteractionData>,
    private val targets: TargetRepository,
    private val state: TargetingState
) : InteractionRepository {
    override fun getInteraction(event: Event): InteractionData? {
        val invocations = targets.getTargets(event)
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