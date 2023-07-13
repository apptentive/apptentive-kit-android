package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.survey.model.SurveyQuestionSet
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class SurveyQuestionSetTest : TestCase() {

    private lateinit var surveyQuestionSet: SurveyQuestionSet

    @Before
    fun testSetup() {
        surveyQuestionSet = SurveyQuestionSet(
            id = "1",
            invokes = listOf(
                InvocationData(
                    interactionId = "2",
                    criteria = mapOf("key" to "value")
                )
            ),
            questions = listOf(),
            buttonText = "Next",
            shouldContinue = true,
        )
    }

    @Test
    fun getId_returnsId() {
        val expected = "1"

        val result = surveyQuestionSet.id

        assertEquals(expected, result)
    }

    @Test
    fun getInvokes_returnsInvokes() {
        val expected = listOf(
            InvocationData(
                interactionId = "2",
                criteria = mapOf("key" to "value")
            )
        )

        val result = surveyQuestionSet.invokes

        assertEquals(expected, result)
    }

    @Test
    fun getButtonText_returnsButtonText() {
        val expected = "Next"

        val result = surveyQuestionSet.buttonText

        assertEquals(expected, result)
    }
}
