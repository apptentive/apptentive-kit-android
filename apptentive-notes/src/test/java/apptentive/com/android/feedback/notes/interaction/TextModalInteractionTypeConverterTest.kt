package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.serialization.json.JsonConverter
import com.google.common.truth.Truth
import org.junit.Test

class TextModalInteractionTypeConverterTest {
    @Test
    fun testConvert() {
        val eq = "eq"
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
                  }
                ]
              }
            }
        """.trimIndent()

        val data = JsonConverter.fromJson<InteractionData>(json)
        val expected = TextModalInteraction(
            id = "id",
            title = "Title",
            body = "Body",
            actions = listOf(
                TextModalInteraction.Action.Invoke(
                    id = "action_id_1",
                    label = "Label 1",
                    invocations = listOf(
                        InvocationData(
                            interactionId = "interaction_1",
                            criteria = mapOf(
                                "interactions/1234567890/invokes/version_name" to mapOf(
                                    eq to 0.0
                                )
                            )
                        )
                    )
                ),
                TextModalInteraction.Action.Dismiss(
                    id = "action_id_2",
                    label = "Label 2"
                )
            )
        )

        val actual = TextModalInteractionTypeConverter().convert(data)
        Truth.assertThat(actual).isEqualTo(expected)
    }
}