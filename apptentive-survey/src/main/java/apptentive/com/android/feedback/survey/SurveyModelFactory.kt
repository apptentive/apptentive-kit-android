package apptentive.com.android.feedback.survey

import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.survey.interaction.DefaultSurveyQuestionConverter
import apptentive.com.android.feedback.survey.interaction.SurveyInteraction
import apptentive.com.android.feedback.survey.model.SurveyModel

internal interface SurveyModelFactory {
    fun getSurveyModel(): SurveyModel
}

internal class SurveyModelFactoryProvider(val context: AndroidEngagementContext,
                                          val interaction: SurveyInteraction) : Provider<SurveyModelFactory> {
    override fun get(): SurveyModelFactory {
        return DefaultSurveyModelFactory(context, interaction)
    }
}

private class DefaultSurveyModelFactory(val context: AndroidEngagementContext,
                                        val interaction: SurveyInteraction) : SurveyModelFactory {
    override fun getSurveyModel(): SurveyModel {
        return SurveyModel(
            questions = interaction.questions.map { config ->
                DefaultSurveyQuestionConverter().convert(
                    config = config,
                    requiredTextMessage = interaction.requiredText
                        ?: context.getString(R.string.apptentive_required)
                )
            },
            name = interaction.name,
            description = interaction.description,
            submitText = interaction.submitText,
            requiredText = interaction.requiredText,
            validationError = interaction.validationError,
            showSuccessMessage = interaction.showSuccessMessage,
            successMessage = interaction.successMessage,
            closeConfirmTitle = interaction.closeConfirmTitle,
            closeConfirmMessage = interaction.closeConfirmMessage,
            closeConfirmCloseText = interaction.closeConfirmCloseText,
            closeConfirmBackText = interaction.closeConfirmBackText
        )

    }
}