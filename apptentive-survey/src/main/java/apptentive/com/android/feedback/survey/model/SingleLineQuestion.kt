package apptentive.com.android.feedback.survey.model

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class SingleLineQuestion(
    id: String,
    title: String,
    validationError: String,
    required: Boolean,
    requiredText: String?,
    instructionsText: String?,
    val freeFormHint: String? = null,
    val multiline: Boolean = false,
    answer: Answer? = null
) : SurveyQuestion<SingleLineQuestion.Answer>(
    id = id,
    title = title,
    validationError = validationError,
    isRequired = required,
    requiredText = requiredText,
    instructionsText = instructionsText,
    answer = answer ?: Answer()
) {
    data class Answer(val value: String? = null) : SurveyQuestionAnswer

    val answerString: String? get() = answer.value

    override fun isValidAnswer(answer: Answer) = !answer.value.isNullOrBlank()

    override fun isAnswered(answer: Answer) = !answer.value.isNullOrEmpty()

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SingleLineQuestion) return false
        if (!super.equals(other)) return false

        if (freeFormHint != other.freeFormHint) return false
        if (multiline != other.multiline) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (freeFormHint?.hashCode() ?: 0)
        result = 31 * result + multiline.hashCode()
        return result
    }

    override fun toString(): String {
        return "SingleLineQuestion(id='$id', title='$title', isRequired=$isRequired, requiredText=$requiredText, instructionsText=$instructionsText, validationError='$validationError', freeformHint=$freeFormHint, multiline=$multiline, answer=$answerString)"
    }

    //endregion
}
