package apptentive.com.android.feedback.inappreview

import apptentive.com.android.TestCase
import apptentive.com.android.core.serialization.json.JsonConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.interactions.inapprating.interaction.InAppReviewInteraction
import apptentive.com.android.feedback.interactions.inapprating.interaction.InAppReviewInteractionTypeConverter
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
