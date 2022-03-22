package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class SurveyInteractionConverterTest: TestCase() {
    @Test
    fun `SurveyInteractionTypeConverter sets null default value for the missing json attributes`() {
        val jsonString = """
            {
                "id": "1234567890",
                "type": "Survey",
                "display_type":"",
                "configuration": {
                    "name": "name",
                    "description": "description",
                    "submit_text": "submit",
                    "required_text":"required_text",
                    "validation_error":"validation_error",
                    "show_success_message":true,
                    "success_message":"success_message",
                    "required":true,
                    "questions":[],
                    "terms_and_conditions": {
                        "label": "labelTest",
                        "link": "linkTest"
                    }
                }
            }
        """

        val expected = SurveyInteraction(
            id = "1234567890",
            name = "name",
            description = "description",
            submitText = "submit",
            requiredText = "required_text",
            validationError = "validation_error",
            showSuccessMessage = true,
            successMessage = "success_message",
            closeConfirmTitle = null,
            closeConfirmMessage = null,
            closeConfirmCloseText = null,
            closeConfirmBackText = null,
            isRequired = true,
            questions = emptyList(),
            termsAndConditions = SurveyInteraction.TermsAndConditions("labelTest", "linkTest")
        )

        val data = JsonConverter.fromJson<InteractionData>(jsonString.trimIndent())
        val actual = SurveyInteractionTypeConverter().convert(data)

        assertEquals(expected.toString(), actual.toString())
    }
}
