package apptentive.com.android.module

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.interactions.enjoyment.EnjoymentDialogInteraction
import apptentive.com.android.feedback.interactions.enjoyment.EnjoymentDialogInteractionLauncher
import apptentive.com.android.feedback.interactions.enjoyment.EnjoymentDialogInteractionTypeConverter

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
