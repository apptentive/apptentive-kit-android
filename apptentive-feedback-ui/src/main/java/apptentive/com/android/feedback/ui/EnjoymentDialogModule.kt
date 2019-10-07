package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.InteractionConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule

// TODO: exclude this class from ProGuard
internal class EnjoymentDialogModule : InteractionModule<EnjoymentDialogInteraction> {
    override val interactionClass = EnjoymentDialogInteraction::class.java

    override fun provideInteractionConverter(): InteractionConverter<EnjoymentDialogInteraction> {
        return EnjoymentDialogInteractionConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<EnjoymentDialogInteraction> {
        return EnjoymentDialogInteractionLauncher()
    }
}