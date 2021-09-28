package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.util.getList
import apptentive.com.android.util.getString
import apptentive.com.android.util.optBoolean
import apptentive.com.android.util.optInt
import apptentive.com.android.util.optString

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
interface SurveyQuestionConverter {
    fun convert(config: SurveyQuestionConfiguration, requiredTextMessage: String): SurveyQuestion<*>
}

internal class DefaultSurveyQuestionConverter : SurveyQuestionConverter {
    override fun convert(
        config: SurveyQuestionConfiguration,
        requiredTextMessage: String
    ): SurveyQuestion<*> {
        val id = config.getString("id")
        val title = config.getString("value")
        val type = config.getString("type")
        val validationError = config.getString("error_message")
        val required = config.optBoolean("required", defaultValue = false)
        val requiredText: String? = if (required) requiredTextMessage else null
        val instructionsText = config.optString("instructions")
        return if (type == "multichoice" || type == "multiselect") {
            MultiChoiceQuestion(
                id = id,
                title = title,
                validationError = validationError,
                required = required,
                requiredText = requiredText,
                instructionsText = instructionsText,
                minSelections = config.optInt("min_selections", defaultValue = 1),
                maxSelections = config.optInt("max_selections", defaultValue = 1),
                allowMultipleAnswers = type == "multiselect",
                answerChoiceConfigs = config.getList("answer_choices").map {
                    @Suppress("UNCHECKED_CAST")
                    convertAnswerChoices(it as Map<String, Any?>)
                }
            )
        } else if (type == "singleline") SingleLineQuestion(
            id = id,
            title = title,
            validationError = validationError,
            required = required,
            requiredText = requiredText,
            instructionsText = instructionsText,
            freeFormHint = config.optString("freeform_hint"),
            multiline = config.optBoolean("multiline")
        )
        else if (type == "range") RangeQuestion(
            id = id,
            title = title,
            validationError = validationError,
            required = required,
            requiredText = requiredText,
            instructionsText = instructionsText,
            min = config.optInt("min", defaultValue = DEFAULT_RANGE_MIN),
            max = config.optInt("max", defaultValue = DEFAULT_RANGE_MAX),
            minLabel = config.optString("min_label"),
            maxLabel = config.optString("max_label")
        )
        else throw IllegalArgumentException("Unsupported question type: $type")
    }

    private fun convertAnswerChoices(configuration: Map<String, Any?>) =
        MultiChoiceQuestion.AnswerChoiceConfiguration(
            id = configuration.getString("id"),
            title = configuration.getString("value"),
            type = MultiChoiceQuestion.ChoiceType.tryParse(configuration.optString("type")),
            hint = configuration.optString("hint")
        )

    companion object {
        private const val DEFAULT_RANGE_MIN = 0
        private const val DEFAULT_RANGE_MAX = 10
    }
}