package apptentive.com.android.module

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.interactions.appstorerating.AppStoreRatingInteraction
import apptentive.com.android.feedback.interactions.appstorerating.AppStoreRatingInteractionLauncher
import apptentive.com.android.feedback.interactions.appstorerating.AppStoreRatingInteractionTypeConverter

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
