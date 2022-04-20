package apptentive.com.android.feedback.survey.model

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class MultiChoiceQuestion(
    id: String,
    title: String,
    validationError: String,
    required: Boolean,
    requiredText: String?,
    instructionsText: String? = null,
    val answerChoiceConfigs: List<AnswerChoiceConfiguration>,
    val allowMultipleAnswers: Boolean,
    val minSelections: Int,
    val maxSelections: Int,
    answer: Answer? = null
) : SurveyQuestion<MultiChoiceQuestion.Answer>(
    id = id,
    title = title,
    validationError = validationError,
    isRequired = required,
    requiredText = requiredText,
    instructionsText = instructionsText,
    answer = answer ?: Answer(
        choices = answerChoiceConfigs.map { config -> Answer.Choice(config.id) }
    )
) {
    val choices: List<Answer.Choice> get() = answer.choices

    enum class ChoiceType {
        select_option, // fixed choice
        select_other; // includes a freeform text entry

        companion object {
            fun tryParse(value: String?): ChoiceType? {
                return if (value != null) valueOf(value) else null
            }
        }
    }

    /**
     * Represents answer choice configuration
     *
     * @param id a string that uniquely identifies the choice when submitting a response to the server
     * @param type answer choice type
     * @param title The text to display alongside the choice (corresponds to one of the Choices text fields on the dashboard).
     * @param hint (when type is select_other): hint text to display in the text entry control when it is empty.
     */
    data class AnswerChoiceConfiguration(
        val type: ChoiceType?,
        val id: String,
        val title: String,
        val hint: String? = null
    )

    data class Answer(val choices: List<Choice> = emptyList()) : SurveyQuestionAnswer {
        data class Choice(val id: String, val checked: Boolean = false, val value: String? = null)
    }

    /** For required questions checks if the response count is within min & max selection boundaries
     * for optional questions checks if there is an answer */
    override fun isValidAnswer(answer: Answer): Boolean {
        val isChecked = { choice: Answer.Choice -> if (choice.checked) 1 else 0 }
        val checkedCount = answer.choices.sumOf(isChecked)
        return checkedCount in minSelections..maxSelections && allChoicesAreValid(answer.choices)
    }

    /** Returns `true` if the optional question has an answer &
     * and it respects the min & max selection boundary/left unanswered */
    override fun validateOptionalQuestion(answer: Answer): Boolean {
        return if (hasAnswer) {
            val isChecked = { choice: Answer.Choice -> if (choice.checked) 1 else 0 }
            val checkedCount = answer.choices.sumOf(isChecked)
            return checkedCount in minSelections..maxSelections && allChoicesAreValid(answer.choices)
        } else true
    }

    override fun isAnswered(answer: Answer): Boolean {
        val isChecked = { choice: Answer.Choice -> if (choice.checked) 1 else 0 }
        val checkedCount = answer.choices.sumOf(isChecked)
        return checkedCount > 0
    }

    private fun allChoicesAreValid(choices: List<Answer.Choice>) =
        // all "checked" choices must be valid
        choices
            .filter { it.checked }
            .all(::isChoiceValid)

    private fun isChoiceValid(choice: Answer.Choice): Boolean {
        val config = answerChoiceConfigs.find { it.id == choice.id }
            ?: throw IllegalArgumentException("Unknown choice id: ${choice.id}")

        return config.type == ChoiceType.select_option ||
            config.type == ChoiceType.select_other && !choice.value.isNullOrBlank()
    }

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultiChoiceQuestion) return false
        if (!super.equals(other)) return false

        if (answerChoiceConfigs != other.answerChoiceConfigs) return false
        if (allowMultipleAnswers != other.allowMultipleAnswers) return false
        if (minSelections != other.minSelections) return false
        if (maxSelections != other.maxSelections) return false
        if (instructionsText != other.instructionsText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + answerChoiceConfigs.hashCode()
        result = 31 * result + allowMultipleAnswers.hashCode()
        result = 31 * result + minSelections
        result = 31 * result + maxSelections
        result = 31 * result + (instructionsText?.hashCode() ?: 0)
        return result
    }

    //endregion
}

/**
 * Creates an updated copy of the answer when a single's choice state changes
 * @param choiceId choice ID
 * @param isChecked indicates if choice is checked or not
 * @param allowMultipleAnswers indicates if only one choice can be selected at all times
 * @param text optional text of "Select other" choices (or null if text wasn't changed)
 */
internal fun MultiChoiceQuestion.Answer.update(
    choiceId: String,
    isChecked: Boolean,
    allowMultipleAnswers: Boolean,
    text: String? = null
): MultiChoiceQuestion.Answer {
    val choices = choices.map { choice ->
        if (choice.id == choiceId) { // we found the question - update the data
            if (text != null) { // text was changed
                choice.copy(checked = isChecked, value = text)
            } else { // only 'checked' state was changed
                choice.copy(checked = isChecked)
            }
        } else if (isChecked && !allowMultipleAnswers && choice.checked) { // we need to "uncheck" everything else in case if multiple answers are not allowed
            choice.copy(checked = false)
        } else { // no modification required
            choice
        }
    }

    return MultiChoiceQuestion.Answer(choices = choices)
}
