package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogInteraction
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogInteractionLauncher
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogInteractionTypeConverter

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