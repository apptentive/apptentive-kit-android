package apptentive.com.android.feedback.survey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.mockExecutors
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.createSingleLineQuestion
import org.junit.Rule
import org.junit.Test

class SurveyViewModelTest : TestCase() {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testListItems() {
        val questionId1 = "id_1"
        val questionId2 = "id_2"
        val surveyValidationError = "Survey is not valid"

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
            surveyValidationError = surveyValidationError
        )

        // observer survey validation error
        viewModel.validationErrorText.observeForever {
            if (it != null) addResult("Validation failed: $it")
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
            "Validation failed: $surveyValidationError", // survey validation error should be shown
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
            )
        )
    }

    private fun createViewModel(
        questions: List<SurveyQuestion<*>>,
        surveyValidationError: String
    ): SurveyViewModel {
        val model = SurveyModel(
            questions = questions,
            validationError = surveyValidationError
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