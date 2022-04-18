package apptentive.com.android.feedback.survey.model

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class RangeQuestion(
    id: String,
    title: String,
    validationError: String,
    required: Boolean,
    requiredText: String?,
    instructionsText: String?,
    val min: Int,
    val max: Int,
    val minLabel: String? = null,
    val maxLabel: String? = null,
    answer: Answer? = null
) : SurveyQuestion<RangeQuestion.Answer>(
    id = id,
    title = title,
    validationError = validationError,
    isRequired = required,
    requiredText = requiredText,
    instructionsText = instructionsText,
    answer = answer ?: Answer()
) {
    data class Answer(val selectedIndex: Int? = null) : SurveyQuestionAnswer

    val selectedValue: Int? get() = answer.selectedIndex

    override fun isValidAnswer(answer: Answer): Boolean {
        return answer.selectedIndex in min..max
    }

    override fun isAnswered(answer: Answer): Boolean {
        return answer.selectedIndex != null
    }

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RangeQuestion) return false
        if (!super.equals(other)) return false

        if (min != other.min) return false
        if (max != other.max) return false
        if (minLabel != other.minLabel) return false
        if (maxLabel != other.maxLabel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + min
        result = 31 * result + max
        result = 31 * result + (minLabel?.hashCode() ?: 0)
        result = 31 * result + (maxLabel?.hashCode() ?: 0)
        return result
    }

    //endregion
}