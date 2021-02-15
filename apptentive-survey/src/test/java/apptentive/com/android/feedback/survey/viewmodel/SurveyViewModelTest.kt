package apptentive.com.android.feedback.survey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.mockExecutors
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.createSingleLineQuestion
import com.google.common.truth.Truth.assertThat
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
        surveyValidationError = surveyValidationErrorState.message)

        // observer survey validation error
        viewModel.surveySubmitMessageState.observeForever {
            if (it != null) addResult(it.message)
        }

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
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            )
        )

        // attempt to submit the survey
        viewModel.submit()

        // check results
        assertResults(
            surveyValidationErrorState.message, // survey validation error should be shown
            "Invalid question: 0", // first invalid question index
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                instructions = "Required",
                validationError = "Question 1 is invalid" // question should be shown as invalid
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            )
        )

        // update answer
        viewModel.updateAnswer(questionId1, "Answer")

        // the question marked as valid
        assertResults(
            SingleLineQuestionListItem(
                id = questionId1,
                title = "title 1",
                text = "Answer",
                instructions = "Required"
            ),
            SingleLineQuestionListItem(
                id = questionId2,
                title = "title 2"
            )
        )

        // submit the survey
        viewModel.submit()

        // check results: unanswered question should be omitted
        assertResults(
            "Submit",
            mapOf(
                questionId1 to SingleLineQuestion.Answer("Answer")
            ),
            "successMessage"
        )
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
            surveyValidationError = surveyValidationErrorState.message
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
        surveyValidationError: String
    ): SurveyViewModel {
        val model = SurveyModel(
            questions = questions,
            name = "name",
            description = "description",
            submitText = "submitText",
            requiredText = "requiredText",
            validationError = surveyValidationError,
            showSuccessMessage = false,
            successMessage = "successMessage"
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