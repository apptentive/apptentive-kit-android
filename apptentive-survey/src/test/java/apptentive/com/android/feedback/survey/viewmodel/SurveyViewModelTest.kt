package apptentive.com.android.feedback.survey.viewmodel

import android.text.SpannedString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.mockExecutors
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.RenderAs
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyAnswerState
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestionSet
import apptentive.com.android.feedback.survey.model.createMultiChoiceQuestionForV12
import apptentive.com.android.feedback.survey.model.createRangeQuestionForV12
import apptentive.com.android.feedback.survey.model.createSingleLineQuestionForV12
import apptentive.com.android.feedback.survey.model.createSurveyModel
import apptentive.com.android.feedback.survey.utils.getValidAnsweredQuestions
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SurveyViewModelTest : TestCase() {
    private val questionId1 = "id_1"
    private val questionId2 = "id_2"
    private val surveyDescription = "description"
    private val submitText = "submitText"
    private val successMessage = "successMessage"

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testListItems() {
        // create a view model
        val viewModel = createViewModel()

        // observer first invalid index
        viewModel.firstInvalidQuestionIndex.observeForever {
            if (it != -1) addResult("Invalid question: $it")
        }

        // observe list items
        viewModel.listItems.observeForever {
            it.forEach(::addResult)
        }

        // check results: both questions are marked as valid
        assertResults(
            SurveyHeaderListItem(surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(submitText, null)
        )

        // attempt to submit the survey
        viewModel.submitListSurvey()

        // check results
        assertResults(
            SurveyHeaderListItem(instructions = surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required",
                validationError = "Question 1 is invalid" // question should be shown as invalid
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(
                buttonTitle = submitText,
                disclaimerText = "disclaimer text",
                messageState = SurveySubmitMessageState(
                    message = "validationError",
                    isValid = false
                )
            ),

            "Invalid question: 1" // first invalid question index (after header)
        )

        // update answer
        viewModel.updateAnswer(questionId1, "Answer")

        // the question marked as valid
        assertResults(
            SurveyHeaderListItem(instructions = surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                text = "Answer",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(buttonTitle = submitText, null)
        )

        // submit the survey
        viewModel.submitListSurvey()

        // check results: unanswered question should be omitted
        assertResults(
            "submit",
            mapOf(
                questionId1 to SurveyAnswerState.Answered(SingleLineQuestion.Answer("Answer")),
                questionId2 to SurveyAnswerState.Empty
            ),
            SurveyHeaderListItem(instructions = surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                text = "Answer",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(
                buttonTitle = submitText,
                disclaimerText = "Disclaimer text",
                messageState = SurveySubmitMessageState(message = successMessage, isValid = true)
            ),
            "close"
        )
    }

    @Test
    fun testPagedSurvey() {
        val viewModel = createViewModel(renderAs = RenderAs.PAGED)
        viewModel.advancePage()
    }

    @Test
    fun testNoSurveyDescription() {
        // header list item should be missing

        // create a view model with empty description
        val viewModel = createViewModel(description = "")

        viewModel.firstInvalidQuestionIndex.observeForever {
            if (it != -1) addResult("Invalid question: $it")
        }

        // observe list items
        viewModel.listItems.observeForever {
            it.forEach(::addResult)
        }

        // check results: both questions are marked as valid
        assertResults(
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(submitText, null)
        )
    }

    @Test
    fun testNoValidationError() {
        // footer item should not contain any message

        // create a view model
        val viewModel = createViewModel()

        // observe list items
        viewModel.listItems.observeForever {
            it.forEach(::addResult)
        }

        // check results: both questions are marked as valid - no validation error
        assertResults(
            SurveyHeaderListItem(surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(submitText, null)
        )

        // attempt to submit the survey
        viewModel.submitListSurvey()

        // check results: First question is invalid - has validation error
        assertResults(
            SurveyHeaderListItem(instructions = surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required",
                validationError = "Question 1 is invalid"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(
                buttonTitle = submitText,
                disclaimerText = "Disclaimer text",
                messageState = SurveySubmitMessageState(
                    message = "validationError",
                    isValid = false
                )
            )
        )

        // update answer
        viewModel.updateAnswer(questionId1, "Answer")

        // the question marked as valid : messageState is reset to null
        assertResults(
            SurveyHeaderListItem(instructions = surveyDescription),
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                text = "Answer",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            ),
            SurveyFooterListItem(buttonTitle = submitText, null, null)
        )
    }

    @Test
    fun testSurveyCancelConfirmationDisplayData() {
        val viewModel = createViewModel(emptyList())

        val expected = SurveyCancelConfirmationDisplay(
            "Close survey?",
            "All the changes will be lost",
            "Back to survey",
            "close"
        )
        assertEquals(expected, viewModel.surveyCancelConfirmationDisplay)
    }

    @Test
    fun testNoConfirmationDialogShowsWhenNothingChanged() {
        // Testing the scenario:
        // When the consumer uses the X button or the back button to close the survey (without having
        // responded to any questions), send a `cancel` event.
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // exit without confirmation dialog
        assertResults("exit", "cancel")
    }

    @Test
    fun testConfirmationDialogShowsAfterSubmitAttempt() {
        // Testing the scenario:
        // When the consumer attempts to submit survey, show confirmation dialog
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to submit the survey
        viewModel.submitListSurvey()

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // show confirmation dialog
        assertResults("confirmation")
    }

    @Test
    fun testConfirmationDialogShowsAfterQuestionAnswered() {
        // Testing the scenario:
        // When the consumer attempts to respond to a question, show confirmation dialog
        val viewModel = createViewModelForExitConfirmationTest()

        // answer the question
        viewModel.updateAnswer("id_1", "My answer")

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // show confirmation dialog
        assertResults("confirmation")
    }

    @Test
    fun testExitWithoutConfirmation() {
        // Testing the scenario:
        // When the consumer is presented with the close confirmation view, send `cancel_partial` if they tap the Close button
        val viewModel = createViewModelForExitConfirmationTest()

        // try to submit
        viewModel.submitListSurvey()

        // answer the question
        viewModel.updateAnswer("id_1", "My answer")

        // attempt to exit without confirmation
        viewModel.exit(showConfirmation = false)

        // exit without confirmation dialog
        assertResults("exit", "cancel_partial")
    }

    @Test
    fun testBackToSurveyIsClickedInConfirmationDialog() {
        // Testing the scenario:
        // When the consumer is presented with the close confirmation view, send `continue_partial` if they tap the Back to Survey button
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to click on back to survey in confirmation dialog
        viewModel.onBackToSurveyFromConfirmationDialog()

        // back to survey event is invoked
        assertResults("back to survey")
    }

    @Test
    fun testAllValidNonRequiredAnswers() {
        // none of the questions contains a valid answer but none is also required so it's fine
        val model = createSurveyModel(
            createSingleLineQuestionForV12(),
            createRangeQuestionForV12(),
            createMultiChoiceQuestionForV12(
                answerChoiceConfigs = listOf(mapOf("id" to "choice_id", "value" to "value", "type" to "select_option"))
            )
        )
        val viewModel = SurveyViewModel(model, executors = mockExecutors, {}, {}, {}, {}, {}, {}, {})
        assertThat(viewModel.allRequiredAnswersAreValid).isTrue()
    }

    @Test
    @Ignore("Need to figure out how to pass the answer to the question set or probably figure out a different way to test this")
    fun testAllValidRequiredAnswers() {
        // all of the questions contains a valid answer and all are required
        val model = createSurveyModel(
            createSingleLineQuestionForV12(required = true),
            createRangeQuestionForV12(selectedIndex = 5, required = true),
            createMultiChoiceQuestionForV12(
                answerChoiceConfigs = listOf(
                    mapOf("id" to "choice_id", "value" to "value", "type" to "select_option")
                ),
                required = true
            )
        )
        val viewModel = SurveyViewModel(model, executors = mockExecutors, {}, {}, {}, {}, {}, {}, {})
        assertThat(viewModel.allRequiredAnswersAreValid).isTrue()
    }

    @Test
    fun testSomeInvalidRequiredAnswers() {
        // some of the questions contains a valid answer and all are required
        val model = createSurveyModel(
            createSingleLineQuestionForV12(required = true),
            createRangeQuestionForV12(selectedIndex = 5, required = true),
            createMultiChoiceQuestionForV12(
                answerChoiceConfigs = listOf(
                    mapOf("id" to "choice_id", "value" to "value", "type" to "select_option")
                ),
                required = true
            )
        )
        val viewModel = SurveyViewModel(model, executors = mockExecutors, {}, {}, {}, {}, {}, {}, {})
        assertThat(viewModel.allRequiredAnswersAreValid).isFalse()
    }

    @Test
    fun testUpdatingAnswer() {
        val questionId = "id"
        val model = createSurveyModel(
            createSingleLineQuestionForV12(id = questionId, required = true)
        )

        val viewModel = SurveyViewModel(model, executors = mockExecutors, {}, {}, {}, {}, {}, {}, {})

        // a single required question contains no answer
        assertThat(viewModel.allRequiredAnswersAreValid).isFalse()

        // update the answer
        viewModel.updateAnswer(
            questionId = questionId,
            value = "New Answer"
        )

        // all answers become valid
        assertThat(viewModel.allRequiredAnswersAreValid).isTrue()

        // remove the answer
        viewModel.updateAnswer(
            questionId = questionId,
            value = ""
        )

        // all answers become invalid
        assertThat(viewModel.allRequiredAnswersAreValid).isFalse()

        // update the answer
        viewModel.updateAnswer(
            questionId = questionId,
            value = "Another Answer"
        )

        // all answers become valid
        assertThat(viewModel.allRequiredAnswersAreValid).isTrue()
    }

    //endregion

    //region First Invalid Index

    @Test
    fun testFirstInvalidQuestion() {
        val model = createSurveyModel(
            createSingleLineQuestionForV12(id = "id_1", required = true),
            createRangeQuestionForV12(id = "id_2", required = true),
            createMultiChoiceQuestionForV12(
                id = "id_3",
                required = true,
                answerChoiceConfigs = listOf(
                    mapOf("id" to "choice_id", "value" to "value", "type" to "select_option")
                )
            )
        )

        val viewModel = SurveyViewModel(model, executors = mockExecutors, {}, {}, {}, {}, {}, {}, {})
        // all questions are invalid
        assertThat(viewModel.getFirstInvalidRequiredQuestionIndex()).isEqualTo(0)

        // answer the first question
        viewModel.updateAnswer(
            questionId = "id_1",
            value = "text"
        )

        // second question becomes first invalid
        assertThat(viewModel.getFirstInvalidRequiredQuestionIndex()).isEqualTo(1)

        // answer the third question
        viewModel.updateAnswer(
            questionId = "id_3",
            choiceId = "choice_id",
            selected = true,
            text = null
        )

        // second question still invalid
        assertThat(viewModel.getFirstInvalidRequiredQuestionIndex()).isEqualTo(1)

        // answer the second question
        viewModel.updateAnswer(
            questionId = "id_2",
            selectedIndex = 5
        )

        // all questions are valid now
        assertThat(viewModel.getFirstInvalidRequiredQuestionIndex()).isEqualTo(-1)

        // remove the answer from the first question
        viewModel.updateAnswer(
            questionId = "id_1",
            value = ""
        )

        // first question becomes first invalid
        assertThat(viewModel.getFirstInvalidRequiredQuestionIndex()).isEqualTo(0)
    }

    @Test
    fun testValidAnsweredQuestions() {
        val question1 = SingleLineQuestion(
            id = "1",
            title = "Question 1",
            validationError = "Invalid answer",
            required = true,
            requiredText = "This question is required",
            instructionsText = "instructions"
        )
        question1.answer = SingleLineQuestion.Answer(value = "Answer 1") // Answered and valid

        val question2 = RangeQuestion(
            id = "2",
            title = "Question 2",
            validationError = "Invalid answer",
            required = true,
            requiredText = "This question is required",
            instructionsText = "instructions",
            min = 1,
            max = 5
        )
        question2.answer = RangeQuestion.Answer(selectedIndex = null) // Empty - Null selectedIndex

        val question3 = MultiChoiceQuestion(
            id = "3",
            title = "Question 3",
            validationError = "Invalid answer",
            required = true,
            requiredText = "This question is required",
            answerChoiceConfigs = listOf(
                MultiChoiceQuestion.AnswerChoiceConfiguration(
                    type = MultiChoiceQuestion.ChoiceType.select_option,
                    id = "a",
                    title = "Option A"
                ),
                MultiChoiceQuestion.AnswerChoiceConfiguration(
                    type = MultiChoiceQuestion.ChoiceType.select_option,
                    id = "b",
                    title = "Option B"
                )
            ),
            allowMultipleAnswers = true,
            minSelections = 1,
            maxSelections = 1
        )
        question3.answer = MultiChoiceQuestion.Answer(
            choices = listOf(
                MultiChoiceQuestion.Answer.Choice(id = "a", checked = true)
            )
        )

        val question4 = SingleLineQuestion(
            id = "4",
            title = "Question 4",
            validationError = "Invalid answer",
            required = true,
            requiredText = "This question is required",
            instructionsText = "instructions"
        )
        question4.answer = SingleLineQuestion.Answer(value = "") //  Empty answer value

        val shownQuestions = listOf(question1, question2, question3, question4)

        val validAnsweredQuestions = getValidAnsweredQuestions(shownQuestions)

        val expectedValidQuestions = listOf(question1, question3)
        assertEquals(expectedValidQuestions, validAnsweredQuestions)
    }

    private fun createViewModelForExitConfirmationTest(): SurveyViewModel {
        val surveyValidationErrorState = SurveySubmitMessageState(
            message = "Survey is not valid",
            isValid = false
        )

        // create a view model
        val viewModel = createViewModel(
            validationError = surveyValidationErrorState.message
        )

        viewModel.exitStream.observeForever {
            // to close survey activity
            addResult("exit")
        }

        viewModel.showConfirmation.observeForever {
            // to show confirmation dialog
            addResult("confirmation")
        }
        return viewModel
    }

    private fun createViewModel(
        questionSet: List<SurveyQuestionSet> = listOf(
            SurveyQuestionSet(
                id = "First",
                invokes = emptyList(),
                questions = listOf(
                    mapOf(
                        "id" to "id_1", "value" to "title 1", "type" to "singleline", "required" to true, "error_message" to "Question 1 is invalid"
                    )
                ),
                buttonText = "NEXT",
                shouldContinue = true,
            ),
            SurveyQuestionSet(
                id = "Second",
                invokes = emptyList(),
                questions = listOf(
                    mapOf(
                        "id" to "id_2", "value" to "title 2", "type" to "singleline", "required" to false, "error_message" to "Question 2 is invalid"
                    )
                ),
                buttonText = "NEXT",
                shouldContinue = true,
            )
        ),
        name: String? = "name",
        description: String? = "description",
        submitText: String = "submitText",
        requiredText: String = "Required",
        validationError: String? = "validationError",
        successMessage: String? = "successMessage",
        renderAs: RenderAs = RenderAs.LIST
    ): SurveyViewModel {
        val model = SurveyModel(
            interactionId = "interaction_id",
            questionSet = questionSet,
            name = name,
            surveyIntroduction = description,
            submitText = submitText,
            requiredText = requiredText,
            validationError = validationError,
            showSuccessMessage = true,
            successMessage = successMessage,
            closeConfirmTitle = "Close survey?",
            closeConfirmMessage = "All the changes will be lost",
            closeConfirmCloseText = "close",
            closeConfirmBackText = "Back to survey",
            termsAndConditionsLinkText = SpannedString("Terms & Conditions"),
            disclaimerText = "Disclaimer text",
            introButtonText = "INTRO",
            successButtonText = "THANKS!",
            renderAs = RenderAs.LIST
        )
        return SurveyViewModel(
            model = model,
            executors = mockExecutors,
            onSubmit = {
                addResult("submit")
                addResult(it)
            },
            recordCurrentAnswer = {
            },
            onCancel = {
                addResult("cancel")
            },
            onCancelPartial = {
                addResult("cancel_partial")
            },
            onClose = {
                addResult("close")
            },
            onBackToSurvey = {
                addResult("back to survey")
            },
            resetCurrentAnswer = {
            }
        )
    }
}
