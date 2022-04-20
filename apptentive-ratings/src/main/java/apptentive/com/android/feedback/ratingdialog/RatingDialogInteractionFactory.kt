package apptentive.com.android.feedback.ratingdialog

import apptentive.com.android.core.Provider

internal interface RatingDialogInteractionFactory {
    fun getRatingDialogInteraction(): RatingDialogInteraction
}

internal class RatingDialogInteractionProvider(val interaction: RatingDialogInteraction) : Provider<RatingDialogInteractionFactory> {
    override fun get(): RatingDialogInteractionFactory {
        return object : RatingDialogInteractionFactory {
            override fun getRatingDialogInteraction(): RatingDialogInteraction = interaction
        }
    }
}
