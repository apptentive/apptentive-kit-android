package apptentive.com.android.module

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.interactions.inapprating.interaction.InAppReviewInteraction
import apptentive.com.android.feedback.interactions.inapprating.interaction.InAppReviewInteractionLauncher
import apptentive.com.android.feedback.interactions.inapprating.interaction.InAppReviewInteractionTypeConverter
import apptentive.com.android.feedback.interactions.inapprating.reviewmanager.DefaultInAppReviewManagerFactory

@Keep
internal class InAppRatingDialogModule : InteractionModule<InAppReviewInteraction> {
    override val interactionClass = InAppReviewInteraction::class.java

    override fun provideInteractionLauncher(): InteractionLauncher<InAppReviewInteraction> {
        return InAppReviewInteractionLauncher(DefaultInAppReviewManagerFactory())
    }

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<InAppReviewInteraction> {
        return InAppReviewInteractionTypeConverter()
    }
}
