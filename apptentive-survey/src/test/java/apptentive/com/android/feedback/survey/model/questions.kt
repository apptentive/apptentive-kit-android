package apptentive.com.android.feedback.survey.model

import apptentive.com.android.feedback.model.InvocationData

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

internal fun createSingleLineQuestionForV12(
    id: String? = null,
    title: String? = null,
    errorMessage: String? = null,
    required: Boolean = false,
    answer: String? = null,
    questionSetID: String = "First",
    invocation: List<InvocationData> = emptyList()
) = SurveyQuestionSet(
    id = questionSetID, invokes = invocation,
    questions = listOf(
        mapOf(
            "id" to (id ?: "id"), "value" to (title ?: "title"), "type" to "singleline", "required" to required, "error_message" to (errorMessage ?: "error_message")
        )
    ),
    buttonText = "NEXT",
    shouldContinue = true,
)

internal fun createRangeQuestionForV12(
    id: String? = null,
    title: String? = null,
    errorMessage: String? = null,
    required: Boolean = false,
    min: Int = 0,
    max: Int = 10,
    minLabel: String? = null,
    maxLabel: String? = null,
    selectedIndex: Int? = null
) = SurveyQuestionSet(
    id = "First", invokes = emptyList(),
    questions = listOf(
        mapOf(
            "id" to (id ?: "id"),
            "value" to (title ?: "title"),
            "type" to "range",
            "required" to required,
            "error_message" to (errorMessage ?: "error_message"),
            "min" to min,
            "max" to max,
            "min_label" to minLabel,
            "max_label" to maxLabel,
            "selected_index" to selectedIndex
        )
    ),
    buttonText = "NEXT",
    shouldContinue = true,
)

internal fun createMultiChoiceQuestionForV12(
    id: String? = null,
    title: String? = null,
    errorMessage: String? = null,
    required: Boolean = false,
    allowMultipleAnswers: Boolean = false,
    minSelections: Int = 1,
    maxSelections: Int = 1,
    answerChoiceConfigs: List<Map<String, Any?>>? = null,
) = SurveyQuestionSet(
    id = "First", invokes = emptyList(),
    questions = listOf(
        mapOf(
            "id" to (id ?: "id"),
            "value" to (title ?: "title"),
            "type" to "multichoice",
            "required" to required,
            "error_message" to (errorMessage ?: "error_message"),
            "min_selections" to minSelections,
            "max_selections" to maxSelections,
            "multiselect" to allowMultipleAnswers,
            "answer_choices" to answerChoiceConfigs
        )
    ),
    buttonText = "NEXT",
    shouldContinue = true,
)
