package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteractionLauncher
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteractionTypeConverter

@Keep
internal class MessageCenterModule : InteractionModule<MessageCenterInteraction> {
    override val interactionClass = MessageCenterInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<MessageCenterInteraction> {
        return MessageCenterInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<MessageCenterInteraction> {
        return MessageCenterInteractionLauncher()
    }
}
