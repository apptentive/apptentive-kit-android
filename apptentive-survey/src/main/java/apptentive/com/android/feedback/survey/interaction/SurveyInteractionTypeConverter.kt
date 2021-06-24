package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.util.getList
import apptentive.com.android.util.optBoolean
import apptentive.com.android.util.optString

internal class SurveyInteractionTypeConverter : InteractionTypeConverter<SurveyInteraction> {
    override fun convert(data: InteractionData): SurveyInteraction {
        val configuration = data.configuration
        return SurveyInteraction(
            id = data.id,
            name = configuration.optString("name"),
            description = configuration.optString("description"),
            submitText = configuration.optString("submit_text"),
            requiredText = configuration.optString("required_text"),
            validationError = configuration.optString("validation_error"),
            showSuccessMessage = configuration.optBoolean("show_success_message"),
            successMessage = configuration.optString("success_message"),
            closeConfirmTitle = configuration.optString("close_confirm_title"),
            closeConfirmMessage = configuration.optString("close_confirm_message"),
            closeConfirmCloseText = configuration.optString("close_confirm_close_text"),
            closeConfirmBackText = configuration.optString("close_confirm_back_text"),
            isRequired = configuration.optBoolean("required"),
            questions = configuration.getList("questions").map {
                @Suppress("UNCHECKED_CAST")
                it as SurveyQuestionConfiguration
            }
        )
    }
}