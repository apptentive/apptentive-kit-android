package apptentive.com.android.feedback.model.interactions

class SurveyInteraction(
    id: String,
    val name: String,
    val description: String?,
    val successMessage: String?,
    val requiredText: String?,
    val submitText: String,
    val validationError: String,
    val questions: List<SurveyQuestion>
) : Interaction(id) {
    val showSuccessMessage = successMessage != null
    val isRequired = requiredText != null
}

