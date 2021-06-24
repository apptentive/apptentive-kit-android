package apptentive.com.android.feedback.survey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.mockExecutors
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.createSingleLineQuestion
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SurveyViewModelTest : TestCase() {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testListItems() {
        val questionId1 = "id_1"
        val questionId2 = "id_2"
        val surveyValidationErrorState = SurveySubmitMessageState(message = "Survey is not valid",
            isValid = false
        )

        val surveyDescription = "description"
        val submitText = "submitText"
        val successMessage = "successMessage"

        // create a view model
        val viewModel = createViewModel(
            questions = listOf(
                // required question
                createSingleLineQuestion(
                    id = questionId1,
                    title = "title 1",
                    required = true,
                    requiredText = "Required",
                    errorMessage = "Question 1 is invalid"
                ),
                // non-required question
                createSingleLineQuestion(
                    id = questionId2,
                    title = "title 2",
                    required = false,
                    errorMessage = "Question 2 is invalid"
                )
            ),
            description = surveyDescription,
            validationError = surveyValidationErrorState.message,
            successMessage = successMessage
        )

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
            SurveyHeaderListItem(instructions=surveyDescription),
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
            SurveyFooterListItem(buttonTitle=submitText, messageState=SurveySubmitMessageState(message="Survey is not valid", isValid=false)),

            "Invalid question: 0" // first invalid question index
        )

        // update answer
        viewModel.updateAnswer(questionId1, "Answer")

        // the question marked as valid
        assertResults(
            SurveyHeaderListItem(instructions=surveyDescription),
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
            SurveyFooterListItem(buttonTitle=submitText)
        )

        // submit the survey
        viewModel.submit()

        // check results: unanswered question should be omitted
        assertResults(
            "Submit",
            mapOf(
                questionId1 to SingleLineQuestion.Answer("Answer")
            ),
            SurveyHeaderListItem(instructions=surveyDescription),
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
            SurveyFooterListItem(buttonTitle=submitText, messageState = SurveySubmitMessageState(message=successMessage, isValid=true))
        )
    }

    @Ignore
    @Test
    fun testNoSurveyDescription() {
        // header list item should be missing
    }

    @Ignore
    @Test
    fun testNoValidationError() {
        // footer item should not contain any message
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
    fun testNoConfirmationWhenNothingChanged() {
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // exit without confirmation
        assertResults("exit")
    }

    @Test
    fun testExitConfirmationAfterSubmitAttempt() {
        val viewModel = createViewModelForExitConfirmationTest()

        // attempt to submit the survey
        viewModel.submit()

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // exit without confirmation
        assertResults("confirmation")
    }

    @Test
    fun testExitConfirmationAfterQuestionAnswered() {
        val viewModel = createViewModelForExitConfirmationTest()

        // answer the question
        viewModel.updateAnswer("id_1", "My answer")

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = true)

        // exit without confirmation
        assertResults("confirmation")
    }

    @Test
    fun testExitWithoutConfirmation() {
        val viewModel = createViewModelForExitConfirmationTest()

        // try to submit
        viewModel.submit()

        // answer the question
        viewModel.updateAnswer("id_1", "My answer")

        // attempt to exit with confirmation
        viewModel.exit(showConfirmation = false)

        // exit without confirmation
        assertResults("exit")
    }

    private fun createViewModelForExitConfirmationTest(): SurveyViewModel {
        val questionId1 = "id_1"
        val questionId2 = "id_2"
        val surveyValidationErrorState = SurveySubmitMessageState(
            message = "Survey is not valid",
            isValid = false
        )

        // create a view model
        val viewModel = createViewModel(
            questions = listOf(
                // required question
                createSingleLineQuestion(
                    id = questionId1,
                    title = "title 1",
                    required = true,
                    requiredText = "Required",
                    errorMessage = "Question 1 is invalid"
                ),
                // non-required question
                createSingleLineQuestion(
                    id = questionId2,
                    title = "title 2",
                    required = false,
                    errorMessage = "Question 2 is invalid"
                )
            ),
            validationError = surveyValidationErrorState.message
        )

        viewModel.exitStream.observeForever {
            addResult("exit")
        }

        viewModel.showConfirmation.observeForever {
            addResult("confirmation")
        }
        return viewModel
    }

    private fun createViewModel(
        questions: List<SurveyQuestion<*>>,
        name: String? = "name",
        description: String? = "description",
        submitText: String? = "submitText",
        requiredText: String? = "requiredText",
        validationError: String? = "validationError",
        successMessage: String? = "successMessage"
    ): SurveyViewModel {
        val model = SurveyModel(
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
                addResult("Submit")
                addResult(it)
            }
        )
    }
}