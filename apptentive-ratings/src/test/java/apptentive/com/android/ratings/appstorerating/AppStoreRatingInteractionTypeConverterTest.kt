package apptentive.com.android.ratings.appstorerating

import apptentive.com.android.feedback.appstorerating.AppStoreRatingInteraction
import apptentive.com.android.feedback.appstorerating.AppStoreRatingInteractionTypeConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStoreRatingInteractionTypeConverterTest {
    @Test
    fun testConvert() {
        val json = """
            {
                "id": "id",
                "type": "AppStoreRating",
                "configuration": {
                   "store_id": "12345678",
                    "method": "magic",
                    "url": "app.store.url"
                }
            }
        """

        val expected = AppStoreRatingInteraction(
            id = "id",
            storeID = "12345678",
            method = "magic",
            url = "app.store.url"
        )

        testConverter(json, expected)
    }

    @Test
    fun testConvertMissingFields() {
        val json = """
            {
                "id": "id",
                "type": "AppStoreRating",
                "configuration": {
                }
            }
        """

        val expected = AppStoreRatingInteraction(
            id = "id",
            storeID = null,
            method = null,
            url = null
        )

        testConverter(json, expected)
    }

    private fun testConverter(
        json: String,
        expected: AppStoreRatingInteraction
    ) {
        val data = JsonConverter.fromJson<InteractionData>(json.trimIndent())
        val actual = AppStoreRatingInteractionTypeConverter().convert(data)
        assertEquals(actual, expected)
    }
}