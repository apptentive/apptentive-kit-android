package apptentive.com.android.feedback.survey

import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.survey.interaction.DefaultSurveyQuestionSetConverter
import apptentive.com.android.feedback.survey.interaction.SurveyInteraction
import apptentive.com.android.feedback.survey.model.RenderAs
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.util.MissingKeyException

internal interface SurveyModelFactory {
    fun getSurveyModel(): SurveyModel
}

internal class SurveyModelFactoryProvider(
    val context: EngagementContext,
    val interaction: SurveyInteraction
) : Provider<SurveyModelFactory> {
    override fun get(): SurveyModelFactory {
        return DefaultSurveyModelFactory(context, interaction)
    }
}

private const val PAGED_SURVEY = "paged"

internal class DefaultSurveyModelFactory(
    private val engagementContext: EngagementContext,
    private val interaction: SurveyInteraction
) : SurveyModelFactory {
    @Throws(MissingKeyException::class)
    override fun getSurveyModel(): SurveyModel {
        return SurveyModel(
            interactionId = interaction.id,
            questionSet = interaction.questionSet.map { config ->
                DefaultSurveyQuestionSetConverter().apply {
                    isPaged = interaction.renderAs == PAGED_SURVEY
                }.convert(configuration = config)
            },
            name = interaction.name,
            surveyIntroduction = interaction.description,
            submitText = interaction.questionSet.map { config ->
                DefaultSurveyQuestionSetConverter().convert(configuration = config)
            }.last().buttonText,
            requiredText = interaction.requiredText ?: engagementContext.getAppActivity().getString(R.string.apptentive_required),
            validationError = interaction.validationError,
            showSuccessMessage = interaction.showSuccessMessage,
            successMessage = interaction.successMessage,
            closeConfirmTitle = interaction.closeConfirmTitle,
            closeConfirmMessage = interaction.closeConfirmMessage,
            closeConfirmCloseText = interaction.closeConfirmCloseText,
            closeConfirmBackText = interaction.closeConfirmBackText,
            termsAndConditionsLinkText = interaction.termsAndConditions?.convertToLink(),
            disclaimerText = interaction.disclaimerText,
            renderAs = if (interaction.renderAs == PAGED_SURVEY) RenderAs.PAGED else RenderAs.LIST,
            introButtonText = interaction.introButtonText,
            successButtonText = interaction.successButtonText
        )
    }
}
