package apptentive.com.android.feedback.survey.model

internal fun createSingleLineQuestion(
    id: String? = null,
    title: String? = null,
    errorMessage: String? = null,
    required: Boolean = false,
    requiredText: String? = null,
    instructionsText: String? = null,
    multiline: Boolean = false,
    freeFormHint: String? = null,
    answer: String? = null
) = SingleLineQuestion(
    id = id ?: "id",
    title = title ?: "title",
    validationError = errorMessage ?: "error_message",
    required = required,
    requiredText = requiredText,
    instructionsText = instructionsText,
    multiline = multiline,
    freeFormHint = freeFormHint,
    answer = if (answer != null) SingleLineQuestion.Answer(answer) else null
)

internal fun createRangeQuestion(
    id: String? = null,
    title: String? = null,
    errorMessage: String? = null,
    required: Boolean = false,
    requiredText: String? = null,
    instructionsText: String? = null,
    min: Int = 0,
    max: Int = 10,
    minLabel: String? = null,
    maxLabel: String? = null,
    selectedIndex: Int? = null
) = RangeQuestion(
    id = id ?: "id",
    title = title ?: "title",
    validationError = errorMessage ?: "error_message",
    required = required,
    requiredText = requiredText,
    instructionsText = instructionsText,
    min = min,
    max = max,
    minLabel = minLabel,
    maxLabel = maxLabel,
    answer = if (selectedIndex != null) RangeQuestion.Answer(selectedIndex) else null
)

internal fun createMultiChoiceQuestion(
    id: String? = null,
    title: String? = null,
    errorMessage: String? = null,
    required: Boolean = false,
    requiredText: String? = null,
    instructionsText: String? = null,
    allowMultipleAnswers: Boolean = false,
    minSelections: Int = 1,
    maxSelections: Int = 1,
    answerChoiceConfigs: List<MultiChoiceQuestion.AnswerChoiceConfiguration>? = null,
    answer: List<MultiChoiceQuestion.Answer.Choice>? = null
): MultiChoiceQuestion {
    return MultiChoiceQuestion(
        id = id ?: "id",
        title = title ?: "title",
        validationError = errorMessage ?: "error_message",
        required = required,
        requiredText = requiredText,
        instructionsText = instructionsText,
        answerChoiceConfigs = answerChoiceConfigs ?: emptyList(),
        allowMultipleAnswers = allowMultipleAnswers,
        minSelections = minSelections,
        maxSelections = maxSelections,
        answer = if (answer != null) MultiChoiceQuestion.Answer(answer) else null
    )
}
