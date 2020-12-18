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
            isRequired = configuration.optBoolean("required"),
            questions = configuration.getList("questions").map {
                @Suppress("UNCHECKED_CAST")
                it as SurveyQuestionConfiguration
            }
        )
    }
}