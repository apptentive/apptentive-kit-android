package apptentive.com.android.feedback.survey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.mockExecutors
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.createSingleLineQuestion
import org.junit.Assert.assertEquals
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
            SurveyFooterListItem(submitText)
        )

        // attempt to submit the survey
        viewModel.submit()

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
            SurveyFooterListItem(buttonTitle = submitText)
        )

        // submit the survey
        viewModel.submit()

        // check results: unanswered question should be omitted
        assertResults(
            "submit",
            mapOf(
                questionId1 to SingleLineQuestion.Answer("Answer")
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
                messageState = SurveySubmitMessageState(message = successMessage, isValid = true)
            ),
            "close"
        )
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
            SurveyFooterListItem(submitText)
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
            SurveyFooterListItem(submitText)
        )

        // attempt to submit the survey
        viewModel.submit()

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
            SurveyFooterListItem(buttonTitle = submitText, messageState = null)
        )
    }

    @Test
    fun testSurveyCancelConfirmationDisplayData() {
        val viewModel = createViewModel(emptyList())

        val expected = SurveyCancelConfirmationDisplay("Close survey?",
            "All the changes will be lost",
            "Back to survey",
            "close"
            )
        assertEquals(expected, viewModel.surveyCancelConfirmationDisplay)
    }

    @Test
    fun testNoConfirmationDialogShowsWhenNothingChanged() {
        //Testing the scenario:
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
        //Testing the scenario:
        //When the consumer attempts to submit survey, show confirmation dialog
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to submit the survey
        viewModel.submit()

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // show confirmation dialog
        assertResults("confirmation")
    }

    @Test
    fun testConfirmationDialogShowsAfterQuestionAnswered() {
        //Testing the scenario:
        //When the consumer attempts to respond to a question, show confirmation dialog
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
        //Testing the scenario:
        // When the consumer is presented with the close confirmation view, send `cancel_partial` if they tap the Close button
        val viewModel = createViewModelForExitConfirmationTest()

        // try to submit
        viewModel.submit()

        // answer the question
        viewModel.updateAnswer("id_1", "My answer")

        // attempt to exit without confirmation
        viewModel.exit(showConfirmation = false)

        // exit without confirmation dialog
        assertResults("exit", "cancel_partial")
    }

    @Test
    fun testBackToSurveyIsClickedInConfirmationDialog() {
        //Testing the scenario:
        //When the consumer is presented with the close confirmation view, send `continue_partial` if they tap the Back to Survey button
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to click on back to survey in confirmation dialog
        viewModel.onBackToSurveyFromConfirmationDialog()

        // back to survey event is invoked
        assertResults("back to survey")
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
            //to close survey activity
            addResult("exit")
        }

        viewModel.showConfirmation.observeForever {
            //to show confirmation dialog
            addResult("confirmation")
        }
        return viewModel
    }

    private fun createViewModel(
        questions: List<SurveyQuestion<*>> = listOf(
            // required question
            createSingleLineQuestion(
                id = "id_1",
                title = "title 1",
                required = true,
                requiredText = "Required",
                errorMessage = "Question 1 is invalid"
            ),
            // non-required question
            createSingleLineQuestion(
                id = "id_2",
                title = "title 2",
                required = false,
                errorMessage = "Question 2 is invalid"
            )
        ),
        name: String? = "name",
        description: String? = "description",
        submitText: String? = "submitText",
        requiredText: String? = "requiredText",
        validationError: String? = "validationError",
        successMessage: String? = "successMessage"
    ): SurveyViewModel {
        val model = SurveyModel(
            interactionId = "interaction_id",
            questions = questions,
            name = name,
            description = description,
            submitText = submitText,
            requiredText = requiredText,
            validationError = validationError,
            showSuccessMessage = true,
            successMessage = successMessage,
            closeConfirmTitle = "Close survey?",
            closeConfirmMessage = "All the changes will be lost",
            closeConfirmCloseText = "close",
            closeConfirmBackText = "Back to survey"
        )
        return SurveyViewModel(
            model = model,
            executors = mockExecutors,
            onSubmit = {
                addResult("submit")
                addResult(it)
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
            }
        )
    }
}