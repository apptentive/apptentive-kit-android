package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.textmodal.TextModalInteraction
import apptentive.com.android.feedback.textmodal.TextModalInteractionTypeConverter
import apptentive.com.android.serialization.json.JsonConverter
import com.google.common.truth.Truth
import org.junit.Test

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
                ]
              }
            }
        """

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
                ),
                TextModalInteraction.Action.Event(
                    id = "action_id_3",
                    label = "Label 3",
                    event = Event.internal("event_1", "TextModal")
                )
            )
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
            actions = listOf(
                TextModalInteraction.Action.Dismiss(
                    id = "action_id_1",
                    label = "Label 1"
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
