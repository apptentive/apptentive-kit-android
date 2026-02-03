package apptentive.com.android.feedback.survey.viewmodel

import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyQuestion

internal interface SurveyQuestionListItemFactory {
    fun createListItem(question: SurveyQuestion<*>, showInvalid: Boolean): SurveyQuestionListItem
}

// the only reason this class exists - some questions might need reading strings from resources
// so we would need to pass context object
internal class DefaultSurveyQuestionListItemFactory :
    SurveyQuestionListItemFactory {
    override fun createListItem(
        question: SurveyQuestion<*>,
        showInvalid: Boolean
    ): SurveyQuestionListItem {
        val instructions = createInstructionText(question)
        val validationError = when {
            // Required & invalid & invalid answer
            showInvalid && question.isRequired && !question.hasValidAnswer -> question.validationError
            // Optional & answered & invalid
            showInvalid && !question.canSubmitOptionalQuestion -> question.validationError
            else -> null
        }
        return when (question) {
            is SingleLineQuestion -> createSingleLineQuestionListItem(question, instructions, validationError)
            is RangeQuestion -> createRangeQuestionListItem(question, instructions, validationError)
            is MultiChoiceQuestion -> createMultiChoiceQuestionListItem(question, instructions, validationError)
            else -> throw IllegalArgumentException("Unsupported type: ${question.javaClass}")
        }
    }

    private fun createRangeQuestionListItem(
        question: RangeQuestion,
        instructions: String?,
        validationError: String?
    ) = RangeQuestionListItem(
        id = question.id,
        title = question.title,
        selectedIndex = question.selectedValue,
        instructions = instructions,
        validationError = validationError,
        min = question.min,
        max = question.max,
        minLabel = question.minLabel,
        maxLabel = question.maxLabel
    )

    private fun createSingleLineQuestionListItem(
        question: SingleLineQuestion,
        instructions: String?,
        validationError: String?
    ) = SingleLineQuestionListItem(
        id = question.id,
        title = question.title,
        text = question.answerString,
        instructions = instructions,
        validationError = validationError,
        freeFormHint = question.freeFormHint,
        multiline = question.multiline
    )

    private fun createMultiChoiceQuestionListItem(
        question: MultiChoiceQuestion,
        instructions: String?,
        validationError: String?
    ) = MultiChoiceQuestionListItem(
        id = question.id,
        title = question.title,
        answerChoices = question.answer.choices.mapIndexed { index: Int, choice: MultiChoiceQuestion.Answer.Choice ->
            val config = question.answerChoiceConfigs[index]
            MultiChoiceQuestionListItem.Answer(
                type = config.type ?: MultiChoiceQuestion.ChoiceType.select_option,
                id = config.id,
                title = config.title,
                hint = config.hint,
                isChecked = choice.checked,
                text = choice.value
            )
        },
        allowMultipleAnswers = question.allowMultipleAnswers,
        instructions = instructions,
        validationError = validationError
    )

    private fun createInstructionText(question: SurveyQuestion<*>): String? {
        val hasRequiredText = question.isRequired && !question.requiredText.isNullOrBlank()
        val hasInstructionText = !question.instructionsText.isNullOrBlank()
        if (hasRequiredText && hasInstructionText) {
            return "${question.requiredText} - ${question.instructionsText}"
        }
        if (hasRequiredText) {
            return question.requiredText
        }
        if (hasInstructionText) {
            return question.instructionsText
        }
        return null
    }
}
