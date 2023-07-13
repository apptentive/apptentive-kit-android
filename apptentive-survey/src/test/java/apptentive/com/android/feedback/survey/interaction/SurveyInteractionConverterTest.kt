package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SurveyInteractionConverterTest : TestCase() {
    private lateinit var surveyInteraction: SurveyInteraction
    @Before
    fun testSetup() {
        surveyInteraction = SurveyInteraction(
            id = "1",
            name = "Survey 1",
            description = "This is a survey",
            renderAs = "default",
            introButtonText = "Start",
            nextText = "Next",
            questionSet = listOf(),
            isRequired = true,
            requiredText = "This field is required",
            validationError = "Invalid input",
            showSuccessMessage = true,
            successMessage = "Survey completed successfully",
            successButtonText = "Finish",
            closeConfirmTitle = "Confirm",
            closeConfirmMessage = "Are you sure you want to close?",
            closeConfirmCloseText = "Close",
            closeConfirmBackText = "Go Back",
            termsAndConditions = SurveyInteraction.TermsAndConditions(
                "Terms",
                "https://example.com/terms"
            ),
            disclaimerText = "Disclaimer"
        )
    }
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
                    "render_as":"paged",
                    "question_sets":[],
                    "terms_and_conditions": {
                        "label": "labelTest",
                        "link": "linkTest"
                    },
                    "disclaimer_text": "Disclaimer text"
                }
            }
        """

        val expected = SurveyInteraction(
            id = "1234567890",
            name = "name",
            description = "description",
            requiredText = "required_text",
            validationError = "validation_error",
            showSuccessMessage = true,
            successMessage = "success_message",
            closeConfirmTitle = null,
            closeConfirmMessage = null,
            closeConfirmCloseText = null,
            closeConfirmBackText = null,
            isRequired = true,
            questionSet = emptyList(),
            termsAndConditions = SurveyInteraction.TermsAndConditions("labelTest", "linkTest"),
            disclaimerText = "Disclaimer text",
            renderAs = "paged",
            nextText = "NEXT",
            introButtonText = "Intro",
            successButtonText = "SUCCESS"
        )

        val data = JsonConverter.fromJson<InteractionData>(jsonString.trimIndent())
        val actual = SurveyInteractionTypeConverter().convert(data)

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `compare json string with the SurveyInteraction`() {
        val expected = "SurveyInteraction(id=1, name=\"Survey 1\", description=\"This is a survey\", " +
            "requiredText=\"This field is required\", validationError=\"Invalid input\", " +
            "showSuccessMessage=true, successMessage=\"Survey completed successfully\", " +
            "closeConfirmTitle=\"Confirm\", closeConfirmMessage=\"Are you sure you want to close?\", " +
            "closeConfirmCloseText=\"Close\", closeConfirmBackText=\"Go Back\", " +
            "isRequired=true, questions=[], " +
            "termsAndConditions=TermsAndConditions(label=Terms, link=https://example.com/terms), " +
            "disclaimerText=Disclaimer)"

        val result = surveyInteraction.toString()

        assertEquals(expected, result)
    }

    @Test
    fun `check comparing different objects returns false`() {
        val other = SurveyInteraction(
            id = "2",
            name = "Survey 2",
            description = "This is another survey",
            renderAs = "default",
            introButtonText = "Start",
            nextText = "Next",
            questionSet = listOf(),
            isRequired = true,
            requiredText = "This field is required",
            validationError = "Invalid input",
            showSuccessMessage = true,
            successMessage = "Survey completed successfully",
            successButtonText = "Finish",
            closeConfirmTitle = "Confirm",
            closeConfirmMessage = "Are you sure you want to close?",
            closeConfirmCloseText = "Close",
            closeConfirmBackText = "Go Back",
            termsAndConditions = SurveyInteraction.TermsAndConditions(
                "Terms",
                "https://example.com/terms"
            ),
            disclaimerText = "Disclaimer"
        )

        val result = surveyInteraction == other

        assertEquals(false, result)
    }
}
