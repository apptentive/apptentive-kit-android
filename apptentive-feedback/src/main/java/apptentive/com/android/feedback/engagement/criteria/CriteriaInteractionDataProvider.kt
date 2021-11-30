package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InteractionDataProvider
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.util.Log

internal class CriteriaInteractionDataProvider(
    private val interactions: Map<InteractionId, InteractionData>,
    private val invocationProvider: InvocationProvider,
    private val state: TargetingState,
    private val usingCustomStoreUrlSkipInAppReviewID: String?
) : InteractionDataProvider {
    override fun getInteractionData(event: Event): InteractionData? {
        val invocations = invocationProvider.getInvocations(event) ?: return null
        return getInteractionData(invocations)
    }

    override fun getInteractionData(invocations: List<Invocation>): InteractionData? {
        val interactionId = getInteractionId(invocations) ?: return null
        return interactions[interactionId]
    }

    private fun getInteractionId(invocations: List<Invocation>): InteractionId? {
        for (invocation in invocations) {
            if (usingCustomStoreUrlSkipInAppReviewID != invocation.interactionId) {
                if (invocation.criteria.isMet(state, verbose = true)) {
                    return invocation.interactionId
                }
            } else {
                Log.d(INTERACTIONS, "Alternate app store is being used. Skipping In App Review Interaction evaluation")
            }
        }
        return null
    }
}
