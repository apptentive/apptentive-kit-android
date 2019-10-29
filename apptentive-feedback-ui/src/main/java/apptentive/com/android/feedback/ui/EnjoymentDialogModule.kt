package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule

// TODO: exclude this class from ProGuard
internal class EnjoymentDialogModule : InteractionModule<EnjoymentDialogInteraction> {
    override val interactionClass = EnjoymentDialogInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<EnjoymentDialogInteraction> {
        return EnjoymentDialogInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<EnjoymentDialogInteraction> {
        return EnjoymentDialogInteractionLauncher()
    }
}