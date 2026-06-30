package apptentive.com.android.feedback.interactions.initiator

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter

internal class InitiatorInteractionTypeConverter : InteractionTypeConverter<InitiatorInteraction> {
    override fun convert(data: InteractionData): InitiatorInteraction {
        return InitiatorInteraction(id = data.id)
    }
}
