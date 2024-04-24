package apptentive.com.android.feedback.survey.viewmodel

import android.text.SpannableString
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.createMultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.createRangeQuestion
import apptentive.com.android.feedback.survey.model.createSingleLineQuestion
import apptentive.com.android.feedback.survey.utils.SpannedUtils
import apptentive.com.android.feedback.utils.HtmlWrapper
import apptentive.com.android.feedback.utils.HtmlWrapper.linkifiedHTMLString
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class DefaultSurveyQuestionListItemFactoryTest {
    @Before
    fun setup() {
        mockkObject(HtmlWrapper)
        every { HtmlWrapper.toHTMLString(any()) } returns SpannableString("TEST")
        every { linkifiedHTMLString(any()) } returns SpannableString("TEST")
        mockkObject(SpannedUtils)
        every { SpannedUtils.isSpannedNotNullOrEmpty(any()) } returns true
        every { SpannedUtils.convertToString(any()) } answers { "TEST" }
    }

    //region SingleLineQuestion
    @Test
    fun testSingleLineValidRequiredQuestionAndPressSubmitButton() = testSingleLineQuestion(
        required = true,
        answer = "answer",
        instructionsText = "Provide your input",
        pressedSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is valid - no need for showing an error
    )

    @Test
    fun testSingleLineValidRequiredQuestionAndDontPressSubmitButton() = testSingleLineQuestion(
        required = true,
        answer = "answer",
        instructionsText = "Provide your input",
        pressedSubmitButton = false,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is valid - no need for showing an error
    )

    @Test
    fun testSingleLineInvalidRequiredQuestionAndPressSubmitButton() = testSingleLineQuestion(
        required = true,
        answer = "", // invalid answer
        instructionsText = "Provide your input",
        pressedSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = true // the answer is invalid - show error message
    )

    @Test
    fun testSingleLineInvalidRequiredQuestionAndDontPressSubmitButton() = testSingleLineQuestion(
        required = true,
        answer = "", // invalid answer
        instructionsText = "Provide your input",
        pressedSubmitButton = false,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is invalid - but we're not required to show it
    )

    @Test
    fun testSingleLineInvalidNonRequiredQuestionAndPressSubmitButton() = testSingleLineQuestion(
        required = false,
        answer = "", // invalid answer
        instructionsText = "Provide your input",
        pressedSubmitButton = true,
        expectedInstructions = "Provide your input",
        expectedInvalid = false // the answer is invalid - but the question is non-required
    )

    @Test
    fun testSingleLineValidRequiredQuestionWithNoInstruction() = testSingleLineQuestion(
        required = true,
        answer = "answer",
        instructionsText = null,
        pressedSubmitButton = false,
        expectedInstructions = "Required",
        expectedInvalid = false
    )

    private fun testSingleLineQuestion(
        required: Boolean,
        answer: String,
        instructionsText: String?,
        pressedSubmitButton: Boolean,
        expectedInstructions: String?,
        expectedInvalid: Boolean
    ) {
        val question = createSingleLineQuestion(
            id = "id",
            title = "title",
            errorMessage = "Validation error",
            required = required,
            requiredText = "Required",
            instructionsText = instructionsText,
            answer = answer,
            freeFormHint = "freeFormHint",
            multiline = true
        )
        val factory = DefaultSurveyQuestionListItemFactory()
        val expected = SingleLineQuestionListItem(
            id = "id",
            title = "title",
            instructions = expectedInstructions,
            validationError = if (expectedInvalid) "Validation error" else null,
            text = answer,
            freeFormHint = "freeFormHint",
            multiline = true
        )
        val actual = factory.createListItem(question, showInvalid = pressedSubmitButton)
        assertThat(actual).isEqualTo(expected)
    }

    //endregion

    //region RangeQuestion

    @Test
    fun testRangeValidRequiredQuestionAndPressSubmitButton() = testRangeQuestion(
        required = true,
        selectedIndex = 5,
        instructionsText = "Provide your input",
        PressSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is valid - no need for showing an error
    )

    @Test
    fun testRangeValidRequiredQuestionAndDontPressSubmitButton() = testRangeQuestion(
        required = true,
        selectedIndex = 5,
        instructionsText = "Provide your input",
        PressSubmitButton = false,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is valid - no need for showing an error
    )

    @Test
    fun testRangeInvalidRequiredQuestionAndPressSubmitButton() = testRangeQuestion(
        required = true,
        selectedIndex = null, // invalid answer
        instructionsText = "Provide your input",
        PressSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = true // the answer is invalid - show error message
    )

    @Test
    fun testRangeInvalidRequiredQuestionAndDontPressSubmitButton() = testRangeQuestion(
        required = true,
        selectedIndex = null, // invalid answer
        instructionsText = "Provide your input",
        PressSubmitButton = false,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is invalid - but we're not required to show it
    )

    @Test
    fun testRangeInvalidNonRequiredQuestionAndPressSubmitButton() = testRangeQuestion(
        required = false,
        selectedIndex = null, // invalid answer
        instructionsText = "Provide your input",
        PressSubmitButton = true,
        expectedInstructions = "Provide your input",
        expectedInvalid = false // the answer is invalid - but the question is non-required
    )

    @Test
    fun testRangeValidRequiredQuestionWithNoInstruction() = testRangeQuestion(
        required = true,
        selectedIndex = 5,
        instructionsText = null,
        PressSubmitButton = false,
        expectedInstructions = "Required",
        expectedInvalid = false
    )

    private fun testRangeQuestion(
        required: Boolean,
        selectedIndex: Int?,
        instructionsText: String?,
        PressSubmitButton: Boolean,
        expectedInstructions: String?,
        expectedInvalid: Boolean
    ) {
        val question = createRangeQuestion(
            id = "id",
            title = "title",
            errorMessage = "Validation error",
            required = required,
            requiredText = "Required",
            instructionsText = instructionsText,
            min = 0,
            max = 10,
            minLabel = "Unlikely",
            maxLabel = "Likely",
            selectedIndex = selectedIndex
        )
        val factory = DefaultSurveyQuestionListItemFactory()
        val expected = RangeQuestionListItem(
            id = "id",
            title = "title",
            instructions = expectedInstructions,
            validationError = if (expectedInvalid) "Validation error" else null,
            min = 0,
            max = 10,
            minLabel = "Unlikely",
            maxLabel = "Likely",
            selectedIndex = selectedIndex
        )
        val actual = factory.createListItem(question, showInvalid = PressSubmitButton)
        assertThat(actual).isEqualTo(expected)
    }

    //endregion

    //region MultichoiceQuestion

    @Test
    fun testMultiChoiceValidRequiredQuestionAndPressSubmitButton() = testMultiChoiceQuestion(
        required = true,
        answers = listOf(
            MockAnswer(selected = false),
            MockAnswer(selected = true, text = "Answer")
        ),
        instructionsText = "Provide your input",
        pressSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = false // the answer is valid - no need for showing an error
    )

    @Test
    fun testMultiChoiceInvalidRequiredQuestionNoSelectionAndPressSubmitButton() = testMultiChoiceQuestion(
        required = true,
        answers = listOf(
            MockAnswer(selected = false),
            MockAnswer(selected = false)
        ),
        instructionsText = "Provide your input",
        pressSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = true // no selections were made
    )

    @Test
    fun testMultiChoiceInvalidRequiredQuestionMissingTextAndPressSubmitButton() = testMultiChoiceQuestion(
        required = true,
        answers = listOf(
            MockAnswer(selected = false),
            MockAnswer(selected = true, text = null)
        ),
        instructionsText = "Provide your input",
        pressSubmitButton = true,
        expectedInstructions = "Required. Provide your input",
        expectedInvalid = true // there's a selection but you did not provide any text
    )

    @Test
    @Ignore
    fun testMultiChoiceValidRequiredQuestionAndDontPressSubmitButton() {
        // if you don't press submit button you should see the question as a valid no matter if it has an answer or not
        TODO()
    }

    @Test
    @Ignore
    fun testMultiChoiceInvalidRequiredQuestionAndDontPressSubmitButton() {
        // if you don't press submit button you should see the question as a valid no matter if it has an answer or not
        TODO()
    }

    @Test
    @Ignore
    fun testMultiChoiceInvalidNonRequiredQuestionAndPressSubmitButton() {
        TODO()
    }

    @Test
    @Ignore
    fun testMultiChoiceValidRequiredQuestionWithNoInstruction() {
        TODO()
    }

    private fun testMultiChoiceQuestion(
        required: Boolean,
        instructionsText: String?,
        answers: List<MockAnswer>?,
        pressSubmitButton: Boolean,
        expectedInstructions: String?,
        expectedInvalid: Boolean
    ) {
        val choices = listOf(
            MultiChoiceQuestion.Answer.Choice(
                id = "choice_1",
                checked = answers?.get(0)?.selected ?: false
            ),
            MultiChoiceQuestion.Answer.Choice(
                id = "choice_2",
                checked = answers?.get(1)?.selected ?: false,
                value = answers?.get(1)?.text
            )
        )
        val question = createMultiChoiceQuestion(
            id = "id",
            title = "title",
            errorMessage = "Validation error",
            required = required,
            requiredText = "Required",
            instructionsText = instructionsText,
            allowMultipleAnswers = true,
            minSelections = 1,
            maxSelections = 2,
            answerChoiceConfigs = listOf(
                MultiChoiceQuestion.AnswerChoiceConfiguration(
                    type = MultiChoiceQuestion.ChoiceType.select_option,
                    id = "choice_1",
                    title = "Answer 1"
                ),
                MultiChoiceQuestion.AnswerChoiceConfiguration(
                    type = MultiChoiceQuestion.ChoiceType.select_other,
                    id = "choice_2",
                    title = "Answer 2",
                    hint = "Hint"
                )
            ),
            answer = choices
        )
        val factory = DefaultSurveyQuestionListItemFactory()
        val expected = MultiChoiceQuestionListItem(
            id = "id",
            title = "title",
            answerChoices = listOf(
                MultiChoiceQuestionListItem.Answer(
                    type = MultiChoiceQuestion.ChoiceType.select_option,
                    id = "choice_1",
                    title = "Answer 1",
                    isChecked = answers?.get(0)?.selected ?: false
                ),
                MultiChoiceQuestionListItem.Answer(
                    type = MultiChoiceQuestion.ChoiceType.select_other,
                    id = "choice_2",
                    title = "Answer 2",
                    isChecked = answers?.get(1)?.selected ?: false,
                    text = answers?.get(1)?.text,
                    hint = "Hint"
                )
            ),
            allowMultipleAnswers = true,
            instructions = expectedInstructions,
            validationError = if (expectedInvalid) "Validation error" else null
        )
        val actual = factory.createListItem(question, showInvalid = pressSubmitButton)
        assertThat(actual).isEqualTo(expected)
    }

    //endregion

    private data class MockAnswer(
        val selected: Boolean,
        val text: String? = null
    )
}
