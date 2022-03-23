package apptentive.com.android.feedback.survey.model

import androidx.annotation.VisibleForTesting

/**
 * Model class to represent survey questions.
 * @param id a string that uniquely identifies the question when submitting a response to the API
 * @param title the text of the question (corresponds to the Question field on the dashboard)
 * @param isRequired whether a response to the question is required for the survey to pass validation (corresponds to the Require Answer checkbox on the dashboard).
 * @param requiredText the text to display with a required question (for example, "Required")
 * @param instructionsText the text to display as an optional instruction (for example, "Select one")
 * @param validationError a textual error message that is read by a screen reader when a question fails to validate
 */
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
abstract class SurveyQuestion<Answer : SurveyQuestionAnswer>(
    val id: String,
    val title: String,
    val isRequired: Boolean,
    val requiredText: String?,
    val instructionsText: String?,
    val validationError: String,
    var answer: Answer
) {
    /** Returns `true` if the question has a valid answer. */
    val hasValidAnswer get() = isValidAnswer(answer)

    /** Returns `true` if the optional question has an answer & passes validation/left unanswered */
    val canSubmitOptionalQuestion get() = validateOptionalQuestion(answer)

    /** Returns `true` if the question has a any answer. */
    val hasAnswer get() = isAnswered(answer)

    /** Checks if the given answer is valid for this specific question */
    protected abstract fun isValidAnswer(answer: Answer): Boolean

    /** Checks if user provided any answer */
    protected abstract fun isAnswered(answer: Answer): Boolean

    /** Validates optional question */
    protected open fun validateOptionalQuestion(answer: Answer): Boolean = true

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveyQuestion<*>) return false

        if (id != other.id) return false
        if (title != other.title) return false
        if (isRequired != other.isRequired) return false
        if (requiredText != other.requiredText) return false
        if (instructionsText != other.instructionsText) return false
        if (validationError != other.validationError) return false
        if (answer != other.answer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + isRequired.hashCode()
        result = 31 * result + (requiredText?.hashCode() ?: 0)
        result = 31 * result + (instructionsText?.hashCode() ?: 0)
        result = 31 * result + validationError.hashCode()
        result = 31 * result + answer.hashCode()
        return result
    }

    //endregion

    override fun toString(): String {
        return "SurveyQuestion(id='$id', title='$title', isRequired=$isRequired, requiredText=$requiredText, instructionsText=$instructionsText, validationError='$validationError', answer=$answer)"
    }
}