package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.survey.viewmodel.RangeQuestionListItem

internal typealias SurveyQuestionConfiguration = Map<String, Any?>

/**
 * @param id interaction id
 * @param name the name that should be displayed at the top of the survey (corresponds to the Title field on the dashboard).
 * @param submitText the text displayed on the submit button (no dashboard setting).
 * @param description a short description/introductory message (corresponds to the Introduction field on the dashboard).
 * @param requiredText the text displayed adjacent to questions that require a response in order to submit the survey (no dashboard setting).
 * @param validationError the text to display when attempting to submit a survey that fails validation (no dashboard setting).
 * @param showSuccessMessage whether to display the next item (corresponds to the Display this message to customers after they complete your survey checkbox on the dashboard).
 * @param successMessage a short message to display after a survey is successfully submitted (corresponds to the Thank You Message field on the dashboard).
 * @param closeConfirmTitle title to display in the survey cancellation confirmation dialog (cancellation confirmation dialog: when the survey is cancelled/closed after partially filled)
 * @param closeConfirmMessage a short message in the survey cancellation confirmation dialog, usually to tell the user that their progress will be lost by cancelling the survey
 * @param closeConfirmCloseText the text displayed on the negative button of the survey cancellation confirmation dialog
 * @param closeConfirmBackText the text displayed on the positive button of the survey cancellation confirmation dialog
 * @param isRequired whether to allow the user to cancel the survey
 * @param questions list of questions
 */
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
class SurveyInteraction(
    id: String,
    val name: String?,
    val description: String?,
    val submitText: String?,
    val requiredText: String?,
    val validationError: String?,
    val showSuccessMessage: Boolean,
    val successMessage: String?,
    val closeConfirmTitle: String?,
    val closeConfirmMessage: String?,
    val closeConfirmCloseText: String?,
    val closeConfirmBackText: String?,
    val isRequired: Boolean,
    val questions: List<SurveyQuestionConfiguration>
) : Interaction(id, type = InteractionType.Survey) {

    override fun toString(): String {
        return javaClass.simpleName +
                "(id=$id, " +
                "name=\"$name\", " +
                "description=\"$description\", " +
                "submitText=\"$submitText\", " +
                "requiredText=\"$requiredText\", " +
                "validationError=\"$validationError\", " +
                "showSuccessMessage=$showSuccessMessage, " +
                "successMessage=\"$successMessage\", " +
                "closeConfirmTitle=\"$closeConfirmTitle\", " +
                "closeConfirmMessage=\"$closeConfirmMessage, " +
                "closeConfirmCloseText=\"$closeConfirmCloseText\", " +
                "closeConfirmBackText=\"$closeConfirmBackText\", " +
                "isRequired=$isRequired, " +
                "questions=$questions)"
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is SurveyInteraction ||
                    name != other.name ||
                    description != other.description ||
                    submitText != other.submitText ||
                    requiredText != other.requiredText ||
                    validationError != other.validationError ||
                    showSuccessMessage != other.showSuccessMessage ||
                    successMessage != other.successMessage ||
                    closeConfirmTitle != other.closeConfirmTitle ||
                    closeConfirmMessage != other.closeConfirmMessage ||
                    closeConfirmBackText != other.closeConfirmBackText ||
                    isRequired != other.isRequired ||
                    questions != other.questions -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (submitText?.hashCode() ?: 0)
        result = 31 * result + (requiredText?.hashCode() ?: 0)
        result = 31 * result + (validationError?.hashCode() ?: 0)
        result = 31 * result + showSuccessMessage.hashCode()
        result = 31 * result + (successMessage?.hashCode() ?: 0)
        result = 31 * result + (closeConfirmTitle?.hashCode() ?: 0)
        result = 31 * result + (closeConfirmMessage?.hashCode() ?: 0)
        result = 31 * result + (closeConfirmCloseText?.hashCode() ?: 0)
        result = 31 * result + (closeConfirmBackText?.hashCode() ?: 0)
        result = 31 * result + isRequired.hashCode()
        result = 31 * result + questions.hashCode()
        return result
    }
}