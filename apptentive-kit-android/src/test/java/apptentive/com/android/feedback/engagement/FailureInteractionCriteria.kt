package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.InteractionCriteria
import apptentive.com.android.feedback.engagement.criteria.TargetingState

internal object FailureInteractionCriteria : InteractionCriteria {
    override fun isMet(state: TargetingState, verbose: Boolean): Boolean {
        throw RuntimeException("Error")
    }
}
