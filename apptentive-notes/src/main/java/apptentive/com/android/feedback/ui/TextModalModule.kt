package apptentive.com.android.feedback.ui

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction
import apptentive.com.android.feedback.notes.interaction.TextModalInteractionLauncher
import apptentive.com.android.feedback.notes.interaction.TextModalInteractionTypeConverter

@Keep
internal class TextModalModule : InteractionModule<TextModalInteraction> {
    override val interactionClass = TextModalInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<TextModalInteraction> {
        return TextModalInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<TextModalInteraction> {
        return TextModalInteractionLauncher()
    }
}

