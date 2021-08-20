package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.rating.interaction.InAppReviewInteraction
import apptentive.com.android.feedback.rating.interaction.InAppReviewInteractionLauncher
import apptentive.com.android.feedback.rating.interaction.InAppReviewInteractionTypeConverter
import apptentive.com.android.feedback.rating.reviewmanager.DefaultInAppReviewManagerFactory

@Keep
internal class InAppReviewModule : InteractionModule<InAppReviewInteraction> {
    override val interactionClass = InAppReviewInteraction::class.java

    override fun provideInteractionLauncher(): InteractionLauncher<InAppReviewInteraction> {
        return InAppReviewInteractionLauncher(DefaultInAppReviewManagerFactory())
    }

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<InAppReviewInteraction> {
        return InAppReviewInteractionTypeConverter()
    }
}
