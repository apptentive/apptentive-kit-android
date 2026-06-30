package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.ui.DialogPosition
import com.google.common.truth.Truth
import org.junit.Test

class EnjoymentDialogInteractionTypeConverterTest {

    @Test
    fun convert_mapsFieldsCorrectly() {
        val json = """
        {
            "id": "interaction_id",
            "type": "EnjoymentDialog",
            "configuration": {
                "title": "Enjoyment Title",
                "yes_text": "Yes",
                "no_text": "No",
                "dismiss_text": "Dismiss",
                "position": "center",
                "vertical_margins": 16
            }
        }
        """

        val expected = EnjoymentDialogInteraction(
            id = "interaction_id",
            title = "Enjoyment Title",
            yesText = "Yes",
            noText = "No",
            dismissText = "Dismiss",
            position = DialogPosition.CENTER,
            verticalMargins = 16
        )

        testConverter(json, expected)
    }

    @Test
    fun convert_handlesMissingOptionalFields() {
        val json = """
        {
            "id": "interaction_id",
            "type": "EnjoymentDialog",
            "configuration": {
                "title": "Enjoyment Title",
                "yes_text": "Yes",
                "no_text": "No"
            }
        }
        """
        val expected = EnjoymentDialogInteraction(
            id = "interaction_id",
            title = "Enjoyment Title",
            yesText = "Yes",
            noText = "No",
            dismissText = null,
            position = DialogPosition.CENTER,
            verticalMargins = null
        )

        testConverter(json, expected)
    }

    private fun testConverter(
        json: String,
        expected: EnjoymentDialogInteraction
    ) {
        val data = JsonConverter.fromJson<InteractionData>(json.trimIndent())
        val actual = EnjoymentDialogInteractionTypeConverter().convert(data)
        Truth.assertThat(actual).isEqualTo(expected)
    }
}
