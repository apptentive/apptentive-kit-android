package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.feedback.engagement.interactions.Interaction

typealias SurveyQuestionConfiguration = Map<String, Any?>

/**
 * @param id interaction id
 * @param name the name that should be displayed at the top of the survey (corresponds to the Title field on the dashboard).
 * @param submitText the text displayed on the submit button (no dashboard setting).
 * @param description a short description/introductory message (corresponds to the Introduction field on the dashboard).
 * @param requiredText the text displayed adjacent to questions that require a response in order to submit the survey (no dashboard setting).
 * @param validationError the text to display when attempting to submit a survey that fails validation (no dashboard setting).
 * @param showSuccessMessage whether to display the next item (corresponds to the Display this message to customers after they complete your survey checkbox on the dashboard).
 * @param successMessage a short message to display after a survey is successfully submitted (corresponds to the Thank You Message field on the dashboard).
 * @param isRequired whether to allow the user to cancel the survey.
 * @param questions list of questions
 */
class SurveyInteraction(
    id: String,
    val name: String?,
    val description: String?,
    val submitText: String?,
    val requiredText: String?,
    val validationError: String?,
    val showSuccessMessage: Boolean,
    val successMessage: String?,
    val isRequired: Boolean,
    val questions: List<SurveyQuestionConfiguration>
) : Interaction(id, type = "Survey")