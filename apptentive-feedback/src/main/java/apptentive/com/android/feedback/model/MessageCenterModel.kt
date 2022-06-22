package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class MessageCenterModel(
    val interactionId: String,
    val title: String?,
    val branding: String?,
    val composer: MessageCenterInteraction.Composer?,
    val greeting: MessageCenterInteraction.Greeting?,
    val status: MessageCenterInteraction.Status?,
    val automatedMessage: MessageCenterInteraction.AutomatedMessage?,
    val errorMessage: MessageCenterInteraction.ErrorMessage?,
    val profile: MessageCenterInteraction.Profile?
)
