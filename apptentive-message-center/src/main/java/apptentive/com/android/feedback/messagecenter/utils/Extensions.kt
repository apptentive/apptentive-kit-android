package apptentive.com.android.feedback.messagecenter.utils

import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction
import apptentive.com.android.feedback.messagecenter.model.MessageCenterModel

internal fun MessageCenterInteraction.convertToMessageCenterModel(): MessageCenterModel = MessageCenterModel(
    messageCenterId,
    title,
    branding,
    composer,
    greeting,
    status,
    automatedMessage,
    errorMessage,
    profile,
)
