package apptentive.com.android.module

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.interactions.textmodal.TextModalInteraction
import apptentive.com.android.feedback.interactions.textmodal.TextModalInteractionLauncher
import apptentive.com.android.feedback.interactions.textmodal.TextModalInteractionTypeConverter

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
