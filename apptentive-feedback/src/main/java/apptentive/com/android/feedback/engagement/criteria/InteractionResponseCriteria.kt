package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class InteractionResponseCriteria(val criteria: Map<String, Any>) : InteractionCriteria {
    override fun isMet(state: TargetingState, verbose: Boolean): Boolean {
        TODO("Not yet implemented")
    }
}
