package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.textmodal.LayoutOptions
import apptentive.com.android.feedback.textmodal.RichContent
import apptentive.com.android.feedback.textmodal.TextModalActionConfiguration
import apptentive.com.android.feedback.textmodal.TextModalInteraction
import apptentive.com.android.feedback.textmodal.TextModalInteractionTypeConverter
import apptentive.com.android.serialization.json.JsonConverter
import com.google.common.truth.Truth
import org.junit.Test
import java.util.Collections.singletonList

class TextModalInteractionTypeConverterTest {
    @Test
    fun testConvert() {
        val json = """
            {
              "id": "id",
              "type": "TextModal",
              "configuration": {
                "title": "Title",
                "body": "Body",
                "actions": [
                  {
                    "id": "action_id_1",
                    "label": "Label 1",
                    "action": "interaction",
                    "invokes": [
                      {
                        "interaction_id": "interaction_1",
                        "criteria": {
                          "interactions/1234567890/invokes/version_name": {
                            "$eq": 0
                          }
                        }
                      }
                    ]
                  },
                  {
                    "id": "action_id_2",
                    "label": "Label 2",
                    "action": "dismiss"
                  },
                  {
                    "id": "action_id_3",
                    "label": "Label 3",
                    "action": "interaction",
                    "event": "com.apptentive#TextModal#event_1"
                  }
                ],
                "image": {
                    "url": "https://variety.com/wp-content/uploads/2022/12/Disney-Plus.png",
                    "layout": "fill",
                    "alt_text": "Disney Logo",
                    "scale": 3
                },
                "max_height": 40
              }
            }
        """

        val expected = TextModalInteraction(
            id = "id",
            title = "Title",
            body = "Body",
            maxHeight = 40,
            actions = arrayListOf<TextModalActionConfiguration>(
                mapOf(
                    "id" to "action_id_1",
                    "label" to "Label 1",
                    "action" to "interaction",
                    "invokes" to singletonList(
                        mapOf(
                            "interaction_id" to "interaction_1",
                            "criteria" to mapOf(
                                "interactions/1234567890/invokes/version_name" to mapOf(eq to 0.0)
                            )
                        )
                    )
                ),
                mapOf(
                    "id" to "action_id_2",
                    "label" to "Label 2",
                    "action" to "dismiss"
                ),
                mapOf(
                    "id" to "action_id_3",
                    "label" to "Label 3",
                    "action" to "interaction",
                    "event" to "com.apptentive#TextModal#event_1"
                )
            ),
            richContent = RichContent(
                url = "https://variety.com/wp-content/uploads/2022/12/Disney-Plus.png",
                layout = LayoutOptions.FULL_WIDTH,
                alternateText = "Disney Logo",
                scale = 3
            ),
        )

        testConverter(json, expected)
    }

    @Test
    fun testConvertMissingFields() {
        val json = """
            {
              "id": "id",
              "type": "TextModal",
              "configuration": {
                "actions": [
                  {
                    "id": "action_id_1",
                    "label": "Label 1",
                    "action": "dismiss"
                  }
                ]
              }
            }
        """

        val expected = TextModalInteraction(
            id = "id",
            title = null,
            body = null,
            richContent = null,
            actions = arrayListOf(
                mapOf(
                    "id" to "action_id_1",
                    "label" to "Label 1",
                    "action" to "dismiss"
                )
            )
        )

        testConverter(json, expected)
    }

    private fun testConverter(
        json: String,
        expected: TextModalInteraction
    ) {
        val data = JsonConverter.fromJson<InteractionData>(json.trimIndent())
        val actual = TextModalInteractionTypeConverter().convert(data)
        Truth.assertThat(actual).isEqualTo(expected)
    }

    companion object {
        private const val eq = "\$eq" // raw string would not allow escaping characters
    }
}
