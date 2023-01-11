package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.textmodal.DefaultTextModalActionConverter
import apptentive.com.android.feedback.textmodal.TextModalActionConfiguration
import apptentive.com.android.feedback.textmodal.TextModalModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Collections

class TextModalActionConverterTests {
    @Test
    fun testConvert() {
        val textModalActionConverter = DefaultTextModalActionConverter()

        val textModalActions = arrayListOf<TextModalActionConfiguration>(
            mapOf(
                "id" to "action_id_1",
                "label" to "Label 1",
                "action" to "interaction",
                "invokes" to Collections.singletonList(
                    mapOf(
                        "interaction_id" to "interaction_1",
                        "criteria" to mapOf(
                            "interactions/1234567890/invokes/version_name" to mapOf(
                                "\$eq" to 0.0
                            )
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
        )

        val expected = listOf(
            TextModalModel.Action.Invoke(
                id = "action_id_1",
                label = "Label 1",
                invocations = listOf(
                    InvocationData(
                        interactionId = "interaction_1",
                        criteria = mapOf(
                            "interactions/1234567890/invokes/version_name" to mapOf(
                                "\$eq" to 0.0
                            )
                        )
                    )
                )
            ),
            TextModalModel.Action.Dismiss(
                id = "action_id_2",
                label = "Label 2"
            ),
            TextModalModel.Action.Event(
                id = "action_id_3",
                label = "Label 3",
                event = Event.internal("event_1", "TextModal")
            )
        )

        val actual = textModalActions.map { textModalActionConverter.convert(it) }

        assertEquals(expected, actual)
    }
}
