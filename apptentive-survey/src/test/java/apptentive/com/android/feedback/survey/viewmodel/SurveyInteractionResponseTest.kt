package apptentive.com.android.feedback.survey.viewmodel

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyAnswerState
import apptentive.com.android.feedback.survey.utils.mapAnswersToResponses
import org.junit.Assert.assertEquals
import org.junit.Test

class SurveyInteractionResponseTest : TestCase() {
    @Test
    fun `test mapAnswersToResponses with MultiChoiceQuestion`() {
        val questionId = "question1"
        val answer = MultiChoiceQuestion.Answer(choices = listOf(MultiChoiceQuestion.Answer.Choice("choice1", checked = true)))
        val answers = mapOf(questionId to SurveyAnswerState.Answered(answer))

        val result = mapAnswersToResponses(answers)

        val expected = mapOf(
            questionId to setOf(InteractionResponse.IdResponse("choice1"))
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test mapAnswersToResponses with SingleLineQuestion`() {
        // String response with non-empty value
        val questionId = "question2"
        val answer = SingleLineQuestion.Answer(value = "Answer")
        val answers = mapOf(questionId to SurveyAnswerState.Answered(answer))

        var result = mapAnswersToResponses(answers)

        var expected = mapOf(
            questionId to setOf(InteractionResponse.StringResponse("Answer"))
        )
        assertEquals(expected, result)

        // String response with empty value
        val questionId2 = "question2"
        val answer2 = SingleLineQuestion.Answer(value = "")
        val answers2 = mapOf(questionId2 to SurveyAnswerState.Answered(answer2))

        result = mapAnswersToResponses(answers2)

        expected = mapOf(
            questionId2 to emptySet()
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test mapAnswersToResponses with RangeQuestion`() {
        val questionId = "question3"
        val answer = RangeQuestion.Answer(selectedIndex = 5)
        val answers = mapOf(questionId to SurveyAnswerState.Answered(answer))

        val result = mapAnswersToResponses(answers)

        val expected = mapOf(
            questionId to setOf(InteractionResponse.LongResponse(5L))
        )
        assertEquals(expected, result)
    }
}
