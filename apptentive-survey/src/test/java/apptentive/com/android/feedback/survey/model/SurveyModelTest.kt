package apptentive.com.android.feedback.survey.model

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion.ChoiceType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SurveyModelTest : TestCase() {
    //region Question Stream

    @Test
    fun testQuestionStream() {
        val model = createSurveyModel(
            createSingleLineQuestion(id = "id_1"),
            createSingleLineQuestion(id = "id_2"),
            createSingleLineQuestion(id = "id_3")
        )
        model.questionsStream.observe {
            it.forEach{ question ->
                addResult(question)
            }
        }

        // the update should be triggered immediately (since it's a behaviour subject)
        assertResults(
            createSingleLineQuestion(id = "id_1"),
            createSingleLineQuestion(id = "id_2"),
            createSingleLineQuestion(id = "id_3")
        )

        // update the second question
        model.updateAnswer(
            questionId = "id_2",
            answer = SingleLineQuestion.Answer("text")
        )

        // should receive the update
        assertResults(
            createSingleLineQuestion(id = "id_1"),
            createSingleLineQuestion(id = "id_2", answer = "text"),
            createSingleLineQuestion(id = "id_3")
        )
    }

    //endregion

    //region All Valid Answers

    @Test
    fun testAllValidNonRequiredAnswers() {
        // none of the questions contains a valid answer but none is also required so it's fine
        val model = createSurveyModel(
            createSingleLineQuestion(),
            createRangeQuestion(),
            createMultiChoiceQuestion(
                answerChoiceConfigs = listOf(
                    MultiChoiceQuestion.AnswerChoiceConfiguration(ChoiceType.select_option, "choice_id", "value")
                )
            )
        )
        assertThat(model.allRequiredAnswersAreValid).isTrue()
    }

    @Test
    fun testAllValidRequiredAnswers() {
        // all of the questions contains a valid answer and all are required
        val model = createSurveyModel(
            createSingleLineQuestion(answer = "text", required = true),
            createRangeQuestion(selectedIndex = 5, required = true),
            createMultiChoiceQuestion(
                answerChoiceConfigs = listOf(
                    MultiChoiceQuestion.AnswerChoiceConfiguration(ChoiceType.select_option, "choice_id", "value")
                ),
                answer = listOf(
                    MultiChoiceQuestion.Answer.Choice("choice_id", checked = true)
                ),
                required = true
            )
        )
        assertThat(model.allRequiredAnswersAreValid).isTrue()
    }

    @Test
    fun testSomeInvalidRequiredAnswers() {
        // some of the questions contains a valid answer and all are required
        val model = createSurveyModel(
            createSingleLineQuestion(answer = "text", required = true),
            createRangeQuestion(selectedIndex = 5, required = true),
            createMultiChoiceQuestion(
                answerChoiceConfigs = listOf(
                    MultiChoiceQuestion.AnswerChoiceConfiguration(ChoiceType.select_option, "choice_id", "value")
                ),
                required = true
            )
        )
        assertThat(model.allRequiredAnswersAreValid).isFalse()
    }

    @Test
    fun testUpdatingAnswer() {
        val questionId = "id"
        val model = createSurveyModel(
            createSingleLineQuestion(id = questionId, required = true)
        )

        // a single required question contains no answer
        assertThat(model.allRequiredAnswersAreValid).isFalse()

        // update the answer
        model.updateAnswer(
            questionId = questionId,
            answer = SingleLineQuestion.Answer("New Answer")
        )

        // all answers become valid
        assertThat(model.allRequiredAnswersAreValid).isTrue()

        // remove the answer
        model.updateAnswer(
            questionId = questionId,
            answer = SingleLineQuestion.Answer("")
        )

        // all answers become invalid
        assertThat(model.allRequiredAnswersAreValid).isFalse()

        // update the answer
        model.updateAnswer(
            questionId = questionId,
            answer = SingleLineQuestion.Answer("Another Answer")
        )

        // all answers become valid
        assertThat(model.allRequiredAnswersAreValid).isTrue()
    }

    //endregion

    //region First Invalid Index

    @Test
    fun testFirstInvalidQuestion() {
        val model = createSurveyModel(
            createSingleLineQuestion(id = "id_1", required = true),
            createRangeQuestion(id = "id_2", required = true),
            createMultiChoiceQuestion(
                id = "id_3",
                required = true,
                answerChoiceConfigs = listOf(
                    MultiChoiceQuestion.AnswerChoiceConfiguration(ChoiceType.select_option, "choice_id", "value")
                )
            )
        )

        // all questions are invalid
        assertThat(model.getFirstInvalidRequiredQuestionIndex()).isEqualTo(0)

        // answer the first question
        model.updateAnswer(
            questionId = "id_1",
            answer = SingleLineQuestion.Answer("text")
        )

        // second question becomes first invalid
        assertThat(model.getFirstInvalidRequiredQuestionIndex()).isEqualTo(1)

        // answer the third question
        model.updateAnswer(
            questionId = "id_3",
            answer = MultiChoiceQuestion.Answer(
                listOf(
                    MultiChoiceQuestion.Answer.Choice("choice_id", checked = true)
                )
            )
        )

        // second question still invalid
        assertThat(model.getFirstInvalidRequiredQuestionIndex()).isEqualTo(1)

        // answer the second question
        model.updateAnswer(
            questionId = "id_2",
            answer = RangeQuestion.Answer(5)
        )

        // all questions are valid now
        assertThat(model.getFirstInvalidRequiredQuestionIndex()).isEqualTo(-1)

        // remove the answer from the first question
        model.updateAnswer(
            questionId = "id_1",
            answer = SingleLineQuestion.Answer("")
        )

        // first question becomes first invalid
        assertThat(model.getFirstInvalidRequiredQuestionIndex()).isEqualTo(0)
    }

    //endregion

    private fun createSurveyModel(vararg questions: SurveyQuestion<*>) = SurveyModel(
        interactionId = "interaction_id",
        questions = questions.toList(),
        name = "name",
        description = "description",
        submitText = "submitText",
        requiredText = "requiredText",
        validationError = "Validation error",
        showSuccessMessage = false,
        successMessage = "successMessage",
        closeConfirmTitle = "Close survey?",
        closeConfirmMessage = "All the changes will be lost",
        closeConfirmCloseText = "close",
        closeConfirmBackText = "Back to survey"
    )
}