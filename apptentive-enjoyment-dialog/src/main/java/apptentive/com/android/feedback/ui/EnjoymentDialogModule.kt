package apptentive.com.android.feedback.ui

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule

@Keep
internal class EnjoymentDialogModule : InteractionModule<EnjoymentDialogInteraction> {
    override val interactionClass = EnjoymentDialogInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<EnjoymentDialogInteraction> {
        return EnjoymentDialogInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<EnjoymentDialogInteraction> {
        return EnjoymentDialogInteractionLauncher()
    }
}