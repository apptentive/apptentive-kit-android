package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.appstorerating.AppStoreRatingInteraction
import apptentive.com.android.feedback.appstorerating.AppStoreRatingInteractionLauncher
import apptentive.com.android.feedback.appstorerating.AppStoreRatingInteractionTypeConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteraction
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionLauncher
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionTypeConverter

@Keep
internal class AppStoreRatingModule : InteractionModule<AppStoreRatingInteraction> {
    override val interactionClass = AppStoreRatingInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<AppStoreRatingInteraction> {
        return AppStoreRatingInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<AppStoreRatingInteraction> {
        return AppStoreRatingInteractionLauncher()
    }
}

