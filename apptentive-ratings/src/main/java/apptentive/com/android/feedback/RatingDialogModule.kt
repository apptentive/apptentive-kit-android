package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteraction
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionLauncher
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionTypeConverter

@Keep
internal class RatingDialogModule : InteractionModule<RatingDialogInteraction> {
    override val interactionClass = RatingDialogInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<RatingDialogInteraction> {
        return RatingDialogInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<RatingDialogInteraction> {
        return RatingDialogInteractionLauncher()
    }
}
