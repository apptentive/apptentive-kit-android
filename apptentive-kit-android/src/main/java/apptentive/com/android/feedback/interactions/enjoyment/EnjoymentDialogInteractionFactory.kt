package apptentive.com.android.feedback.interactions.enjoyment

import apptentive.com.android.core.Provider

internal interface EnjoymentDialogInteractionFactory {
    fun getEnjoymentDialogInteraction(): EnjoymentDialogModel
}

internal class EnjoymentDialogInteractionProvider(val interaction: EnjoymentDialogInteraction, val whereEvent: String?) : Provider<EnjoymentDialogInteractionFactory> {
    override fun get(): EnjoymentDialogInteractionFactory = object : EnjoymentDialogInteractionFactory {
        override fun getEnjoymentDialogInteraction(): EnjoymentDialogModel {
            return EnjoymentDialogModel(interaction, whereEvent)
        }
    }
}
