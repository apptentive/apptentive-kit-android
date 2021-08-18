package apptentive.com.android.ratings.ratingdialog

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteraction
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionTypeConverter
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class RatingDialogInteractionTypeConverterTest {
    @Test
    fun testConvert() {
        val json = """
            {
                "id": "id",
                "type": "RatingDialog",
                "configuration": {
                    "title": "Title",
                    "body": "Body",
                    "rate_text": "Rate",
                    "remind_text": "Remind",
                    "decline_text": "Decline"
                }
            }
        """

        val expected = RatingDialogInteraction(
            id = "id",
            title = "Title",
            body = "Body",
            rateText = "Rate",
            remindText = "Remind",
            declineText = "Decline"
        )

        testConverter(json, expected)
    }

    @Test
    fun testConvertMissingFields() {
        val json = """
            {
                "id": "id",
                "type": "RatingDialog",
                "configuration": {
                }
            }
        """

        val expected = RatingDialogInteraction(
            id = "id",
            title = null,
            body = null,
            rateText = null,
            remindText = null,
            declineText = null
        )

        testConverter(json, expected)
    }

    private fun testConverter(
        json: String,
        expected: RatingDialogInteraction
    ) {
        val data = JsonConverter.fromJson<InteractionData>(json.trimIndent())
        val actual = RatingDialogInteractionTypeConverter().convert(data)
        assertEquals(actual, expected)
    }
}