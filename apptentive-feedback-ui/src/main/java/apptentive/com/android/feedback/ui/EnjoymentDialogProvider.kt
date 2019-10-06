package apptentive.com.android.feedback.ui

import android.content.Context
import apptentive.com.android.feedback.engagement.interactions.InteractionConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionProvider

internal class EnjoymentDialogProvider : InteractionProvider<EnjoymentDialogInteraction> {
    private val converter: InteractionConverter<EnjoymentDialogInteraction> by lazy {
        EnjoymentDialogInteractionConverter()
    }

    override fun provideConverter() = converter

    override fun provideLauncher(context: Context) = EnjoymentDialogInteractionLauncher(context)
}