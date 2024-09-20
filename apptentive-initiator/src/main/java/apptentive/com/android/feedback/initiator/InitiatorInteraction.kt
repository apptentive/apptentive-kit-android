package apptentive.com.android.feedback.initiator.interaction

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType

internal class InitiatorInteraction(id: InteractionId) : Interaction(id, InteractionType.Initiator) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InitiatorInteraction) return false
        if (!super.equals(other)) return false
        return true
    }
}
