package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.survey.interaction.SurveyInteraction.TermsAndConditions
import apptentive.com.android.util.getList
import apptentive.com.android.util.getString
import apptentive.com.android.util.optBoolean
import apptentive.com.android.util.optMap
import apptentive.com.android.util.optString

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class SurveyInteractionTypeConverter : InteractionTypeConverter<SurveyInteraction> {
    override fun convert(data: InteractionData): SurveyInteraction {
        val configuration = data.configuration
        return SurveyInteraction(
            id = data.id,
            name = configuration.optString("name"),
            description = configuration.optString("description"),
            requiredText = configuration.optString("required_text"),
            validationError = configuration.optString("validation_error"),
            showSuccessMessage = configuration.optBoolean("show_success_message"),
            successMessage = configuration.optString("success_message"),
            closeConfirmTitle = configuration.optString("close_confirm_title"),
            closeConfirmMessage = configuration.optString("close_confirm_message"),
            closeConfirmCloseText = configuration.optString("close_confirm_close_text"),
            closeConfirmBackText = configuration.optString("close_confirm_back_text"),
            isRequired = configuration.optBoolean("required"),
            questionSet = configuration.getList("question_sets").map {
                @Suppress("UNCHECKED_CAST")
                it as SurveyQuestionSetConfiguration
            },
            termsAndConditions = configuration.optMap("terms_and_conditions")?.convertTermsAndConditions(),
            renderAs = configuration.getString("render_as"),
            nextText = configuration.optString("next_text"),
            introButtonText = configuration.optString("intro_button_text"),
            successButtonText = configuration.optString("success_button_text"),
            disclaimerText = configuration.optString("disclaimer_text"),
        )
    }

    private fun Map<String, Any?>.convertTermsAndConditions(): TermsAndConditions {
        return TermsAndConditions(
            label = optString("label"),
            link = optString("link")
        )
    }
}
