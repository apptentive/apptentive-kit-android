package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.core.Provider

internal interface EnjoymentDialogInteractionFactory {
    fun getEnjoymentDialogInteraction(): EnjoymentDialogInteraction
}

internal class EnjoymentDialogInteractionProvider(val interaction: EnjoymentDialogInteraction) : Provider<EnjoymentDialogInteractionFactory> {
    override fun get(): EnjoymentDialogInteractionFactory = object : EnjoymentDialogInteractionFactory {
        override fun getEnjoymentDialogInteraction(): EnjoymentDialogInteraction {
            return interaction
        }
    }
}
