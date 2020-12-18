package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion.AnswerChoiceConfiguration
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion.ChoiceType
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SurveyQuestionConverterTest : TestCase() {
    //region SingleLineQuestion

    @Test
    fun testSingleLineQuestion() {
        val source = mapOf<String, Any?>(
            "type" to "singleline",
            "id" to "single-line-question",
            "value" to "Is there anything you'd like to add?",
            "error_message" to "Error - There was a problem with your text answer.",
            "required" to true,
            "freeform_hint" to "Please provide your answer",
            "multiline" to true,
            "instructions" to "Instructions"
        )
        val expected = SingleLineQuestion(
            id = "single-line-question",
            title = "Is there anything you'd like to add?",
            validationError = "Error - There was a problem with your text answer.",
            required = true,
            requiredText = "Required",
            freeFormHint = "Please provide your answer",
            multiline = true,
            instructionsText = "Instructions"
        )
        val actual = DefaultSurveyQuestionConverter().convert(source, "Required")
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun testSingleLineQuestionPartial() {
        val source = mapOf<String, Any?>(
            "type" to "singleline",
            "id" to "single-line-question",
            "value" to "Is there anything you'd like to add?",
            "error_message" to "Error - There was a problem with your text answer.",
            "required" to false
        )
        val expected = SingleLineQuestion(
            id = "single-line-question",
            title = "Is there anything you'd like to add?",
            validationError = "Error - There was a problem with your text answer.",
            required = false,
            requiredText = null,
            freeFormHint = null,
            multiline = false,
            instructionsText = null
        )
        val actual = DefaultSurveyQuestionConverter().convert(source, "Required")
        assertThat(expected).isEqualTo(actual)
    }

    //endregion

    //region RangeQuestion

    @Test
    fun testRangeQuestion() {
        val source = mapOf<String, Any?>(
            "type" to "range",
            "id" to "range-question",
            "value" to "How likely is it that you would recommend our app to a friend or colleague?",
            "error_message" to "Error - There was a problem with your answer.",
            "required" to true,
            "instructions" to "Instructions",
            "min" to 0,
            "max" to 10,
            "min_label" to "Not at all likely",
            "max_label" to "Extremely likely"
        )
        val expected = RangeQuestion(
            id = "range-question",
            title = "How likely is it that you would recommend our app to a friend or colleague?",
            validationError = "Error - There was a problem with your answer.",
            required = true,
            requiredText = "Required",
            instructionsText = "Instructions",
            min = 0,
            max = 10,
            minLabel = "Not at all likely",
            maxLabel = "Extremely likely"
        )
        val actual = DefaultSurveyQuestionConverter().convert(source, "Required")
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun testRangeQuestionPartial() {
        val source = mapOf<String, Any?>(
            "type" to "range",
            "id" to "range-question",
            "value" to "How likely is it that you would recommend our app to a friend or colleague?",
            "error_message" to "Error - There was a problem with your answer.",
            "required" to false
        )
        val expected = RangeQuestion(
            id = "range-question",
            title = "How likely is it that you would recommend our app to a friend or colleague?",
            validationError = "Error - There was a problem with your answer.",
            required = false,
            requiredText = null,
            instructionsText = null,
            min = 0,
            max = 10,
            minLabel = null,
            maxLabel = null
        )
        val actual = DefaultSurveyQuestionConverter().convert(source, "Required")
        assertThat(expected).isEqualTo(actual)
    }

    //endregion

    //region RangeQuestion

    @Test
    fun testMultiChoiceQuestion() {
        val source = mapOf<String, Any?>(
            "type" to "multichoice",
            "id" to "multichoice-question",
            "value" to "Multichoice Optional",
            "error_message" to "Error - There was a problem with your single-select answer.",
            "required" to true,
            "instructions" to "select one",
            "answer_choices" to listOf(
                mapOf<String, Any?>(
                    "id" to "choice_1",
                    "value" to "Title 1",
                    "type" to "select_option"
                ),
                mapOf<String, Any?>(
                    "id" to "choice_2",
                    "value" to "Title 2",
                    "type" to "select_option"
                ),
                mapOf<String, Any?>(
                    "id" to "choice_3",
                    "value" to "Other",
                    "type" to "select_other",
                    "hint" to "Hint"
                )
            )
        )
        val expected = MultiChoiceQuestion(
            id = "multichoice-question",
            title = "Multichoice Optional",
            validationError = "Error - There was a problem with your single-select answer.",
            required = true,
            requiredText = "Required",
            instructionsText = "select one",
            allowMultipleAnswers = false,
            minSelections = 1,
            maxSelections = 1,
            answerChoiceConfigs = listOf(
                AnswerChoiceConfiguration(
                    id = "choice_1",
                    title = "Title 1",
                    type = ChoiceType.select_option
                ),
                AnswerChoiceConfiguration(
                    id = "choice_2",
                    title = "Title 2",
                    type = ChoiceType.select_option
                ),
                AnswerChoiceConfiguration(
                    id = "choice_3",
                    title = "Other",
                    type = ChoiceType.select_other,
                    hint = "Hint"
                )
            )
        )
        val actual = DefaultSurveyQuestionConverter().convert(source, "Required")
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun testMultiSelectQuestion() {
        val source = mapOf<String, Any?>(
            "type" to "multiselect",
            "id" to "multichoice-question",
            "value" to "Multichoice Optional",
            "error_message" to "Error - There was a problem with your single-select answer.",
            "required" to true,
            "instructions" to "select one",
            "min_selections" to 2,
            "max_selections" to 3,
            "answer_choices" to listOf(
                mapOf<String, Any?>(
                    "id" to "choice_1",
                    "value" to "Title 1",
                    "type" to "select_option"
                ),
                mapOf<String, Any?>(
                    "id" to "choice_2",
                    "value" to "Title 2",
                    "type" to "select_option"
                ),
                mapOf<String, Any?>(
                    "id" to "choice_3",
                    "value" to "Other",
                    "type" to "select_other",
                    "hint" to "Hint"
                )
            )
        )
        val expected = MultiChoiceQuestion(
            id = "multichoice-question",
            title = "Multichoice Optional",
            validationError = "Error - There was a problem with your single-select answer.",
            required = true,
            requiredText = "Required",
            instructionsText = "select one",
            allowMultipleAnswers = true,
            minSelections = 2,
            maxSelections = 3,
            answerChoiceConfigs = listOf(
                AnswerChoiceConfiguration(
                    id = "choice_1",
                    title = "Title 1",
                    type = ChoiceType.select_option
                ),
                AnswerChoiceConfiguration(
                    id = "choice_2",
                    title = "Title 2",
                    type = ChoiceType.select_option
                ),
                AnswerChoiceConfiguration(
                    id = "choice_3",
                    title = "Other",
                    type = ChoiceType.select_other,
                    hint = "Hint"
                )
            )
        )
        val actual = DefaultSurveyQuestionConverter().convert(source, "Required")
        assertThat(expected).isEqualTo(actual)
    }

    //endregion
}