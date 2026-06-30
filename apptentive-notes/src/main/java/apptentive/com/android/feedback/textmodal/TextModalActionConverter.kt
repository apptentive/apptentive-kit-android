package apptentive.com.android.feedback.textmodal

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.util.getList
import apptentive.com.android.util.getMap
import apptentive.com.android.util.getString
import apptentive.com.android.util.optString

internal interface TextModalActionConverter {
    fun convert(config: TextModalActionConfiguration): TextModalModel.Action
}

internal class DefaultTextModalActionConverter : TextModalActionConverter {
    override fun convert(config: TextModalActionConfiguration): TextModalModel.Action {
        val id = config.getString("id")
        val label = config.getString("label")
        val action = config.getString("action")
        @Suppress("UNCHECKED_CAST")
        return when (action) {
            "interaction" -> {
                val event = config.optString("event")
                if (event != null) {
                    TextModalModel.Action.Event(
                        id = id,
                        label = label,
                        event = Event.parse(event)
                    )
                } else {
                    TextModalModel.Action.Invoke(
                        id = id,
                        label = label,
                        invocations = (config.getList("invokes") as List<Map<String, Any?>>).map(::convertInvocation)
                    )
                }
            }
            "dismiss" -> TextModalModel.Action.Dismiss(
                id = id,
                label = label
            )
            else -> throw IllegalArgumentException("Unexpected action: $action")
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun convertInvocation(config: Map<String, Any?>) = InvocationData(
    interactionId = config.getString("interaction_id"),
    criteria = config.getMap("criteria") as Map<String, Any>
)
