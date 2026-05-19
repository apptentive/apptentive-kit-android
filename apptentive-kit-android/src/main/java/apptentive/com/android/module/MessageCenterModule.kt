package apptentive.com.android.module

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.interactions.messagecenter.interaction.MessageCenterInteractionLauncher
import apptentive.com.android.feedback.interactions.messagecenter.interaction.MessageCenterInteractionTypeConverter
import apptentive.com.android.feedback.message.MessageCenterInteraction

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
