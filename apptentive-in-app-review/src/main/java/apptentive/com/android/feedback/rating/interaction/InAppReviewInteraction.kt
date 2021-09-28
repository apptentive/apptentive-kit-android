package apptentive.com.android.feedback.rating.interaction

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType

internal class InAppReviewInteraction(id: InteractionId) : Interaction(id, InteractionType.GoogleInAppReview) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InAppReviewInteraction) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
