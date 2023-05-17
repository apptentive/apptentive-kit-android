package apptentive.com.android.feedback.survey.model

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.MockTimeRule
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.toProperJson
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class SurveyResponsePayloadTest : TestCase() {
    @get:Rule
    val timeRule = MockTimeRule(currentTime = 1000.0, utcOffset = -18000)

    @Test
    fun testEventPayloadData() {
        val surveyId = "survey_id"

        val payload = SurveyResponsePayload.fromAnswers(
            id = surveyId,
            answers = mapOf(
                "1" to SingleLineQuestion.Answer(value = "answer"),
                "2" to RangeQuestion.Answer(selectedIndex = 5),
                "3" to MultiChoiceQuestion.Answer(
                    choices = listOf(
                        MultiChoiceQuestion.Answer.Choice(id = "choice_1", checked = true),
                        MultiChoiceQuestion.Answer.Choice(id = "choice_2", checked = false),
                        MultiChoiceQuestion.Answer.Choice(id = "choice_3", value = "text", checked = true)
                    )
                )
            )
        )

        val expectedJson = toProperJson(
            "{" +
                "'response':{" +
                "'id':'$surveyId'," +
                "'answers':{" +
                "'1':[{'value':'answer'}]," +
                "'2':[{'value':5}]," +
                "'3':[{'id':'choice_1'},{'id':'choice_3','value':'text'}]" +
                "}," +
                "'session_id':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'," +
                "'client_created_at':1000.0," +
                "'client_created_at_utc_offset':-18000," +
                "'nonce':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'" +
                "}" +
                "}"
        )

        val expected = PayloadData(
            nonce = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
            type = PayloadType.SurveyResponse,
            path = "/conversations/:conversation_id/surveys/$surveyId/responses",
            method = HttpMethod.POST,
            mediaType = MediaType.applicationJson,
            data = expectedJson.toByteArray()
        )
        val actual = payload.toPayloadData()
        assertThat(actual).isEqualTo(expected)
    }
}
