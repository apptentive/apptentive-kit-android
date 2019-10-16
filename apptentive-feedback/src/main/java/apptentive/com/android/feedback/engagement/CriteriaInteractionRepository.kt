package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.Target
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
        val targets = targets.getTargets(event)
        if (targets == null) {
            // FIXME: log statement
            return null
        }

        val interactionId = getInteractionId(targets)
        if (interactionId == null) {
            // FIXME: log statement
            return null
        }

        return interactions[interactionId]
    }

    private fun getInteractionId(targets: List<Target>): InteractionId? {
        // FIXME: exception handling
        for (target in targets) {
            if (target.criteria.isMet(state)) {
                return target.interactionId
            }
        }
        return null
    }
}