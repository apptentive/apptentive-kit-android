package apptentive.com.android.feedback

import androidx.annotation.Keep
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.survey.interaction.SurveyInteraction
import apptentive.com.android.feedback.survey.interaction.SurveyInteractionLauncher
import apptentive.com.android.feedback.survey.interaction.SurveyInteractionTypeConverter

@Keep
internal class SurveyModule : InteractionModule<SurveyInteraction> {
    override val interactionClass = SurveyInteraction::class.java

    override fun provideInteractionTypeConverter(): InteractionTypeConverter<SurveyInteraction> {
        return SurveyInteractionTypeConverter()
    }

    override fun provideInteractionLauncher(): InteractionLauncher<SurveyInteraction> {
        return SurveyInteractionLauncher()
    }
}
