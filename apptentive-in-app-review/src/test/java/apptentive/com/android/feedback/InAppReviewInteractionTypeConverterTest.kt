package apptentive.com.android.feedback

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.rating.interaction.InAppReviewInteraction
import apptentive.com.android.feedback.rating.interaction.InAppReviewInteractionTypeConverter
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert
import org.junit.Test

class InAppReviewInteractionTypeConverterTest : TestCase() {
    @Test
    fun testConvert() {
        val json = """
            {
                "id": "InAppReviewId",
                "type": "InAppRatingDialog",
                "configuration": {
                }
            }
        """

        val expected = InAppReviewInteraction(
            id = "InAppReviewId",
        )

        testConverter(json, expected)
    }

    private fun testConverter(
        json: String,
        expected: InAppReviewInteraction
    ) {
        val data = JsonConverter.fromJson<InteractionData>(json.trimIndent())
        val actual = InAppReviewInteractionTypeConverter().convert(data)
        Assert.assertEquals(actual, expected)
    }
}
