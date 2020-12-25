package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Test

class TextModalInteractionTypeConverterTest {
    @Test
    fun convert() {
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
                        "criteria": {}
                      }
                    ]
                  },
                  {
                    "id": "action_id_2",
                    "label": "Label 2",
                    "action": "dismiss"
                  }
                ]
              }
            }
        """.trimIndent()

        val data = JsonConverter.fromJson<InteractionData>(json)
        val expected = TextModalInteraction(
            id = "id",
            title = "Title",
            body = "Body"
        )
    }
}