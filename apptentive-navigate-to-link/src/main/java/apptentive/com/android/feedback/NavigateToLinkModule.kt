package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.link.NavigateToLinkInteraction
import apptentive.com.android.feedback.link.NavigateToLinkInteractionLauncher
import apptentive.com.android.feedback.link.NavigateToLinkInteractionTypeConverter

@Keep
internal class NavigateToLinkModule : InteractionModule<NavigateToLinkInteraction> {
    override val interactionClass = NavigateToLinkInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<NavigateToLinkInteraction> {
        return NavigateToLinkInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<NavigateToLinkInteraction> {
        return NavigateToLinkInteractionLauncher()
    }
}