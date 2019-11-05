package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.InteractionCriteria
import apptentive.com.android.feedback.engagement.criteria.TargetingState

object FailureInteractionCriteria : InteractionCriteria {
    override fun isMet(state: TargetingState): Boolean {
        throw RuntimeException("Error")
    }
}