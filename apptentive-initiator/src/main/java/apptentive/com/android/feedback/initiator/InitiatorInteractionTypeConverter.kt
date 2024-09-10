package apptentive.com.android.feedback.initiator.interaction

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter

internal class InitiatorInteractionTypeConverter : InteractionTypeConverter<InitiatorInteraction> {
    override fun convert(data: InteractionData): InitiatorInteraction {
        val configuration = data.configuration
        return InitiatorInteraction(id = data.id)
    }
}
