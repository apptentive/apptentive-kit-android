package apptentive.com.android.feedback.notes.viewmodel

import apptentive.com.android.core.Callback
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction

class TextModalViewModel(
    private val context: EngagementContext,
    private val interaction: TextModalInteraction
) {
    var onDismiss: Callback? = null

    fun invokeAction(id: String) {
        context.executors.state.execute {
            val position = indexOfAction(id)
            when (val action = interaction.actions[position]) {
                is TextModalInteraction.Action.Dismiss -> {
                    engageCodePoint(CODE_POINT_DISMISS)
                }
                is TextModalInteraction.Action.Invoke -> {
                    // engage
                    val data = mapOf<String, Any?>(
                        "action_id" to action.id,
                        "label" to action.label,
                        "position" to position,
                        "invoked_interaction_id" to null
                    )
                    engageCodePoint(CODE_POINT_INTERACTION, data)

                    // run invocation
                    val result = context.engage(action.invocations)
                    if (result !is EngagementResult.Success) {
                        // FIXME: error message
                    }
                }
                is TextModalInteraction.Action.Event -> {
                    val result = context.engage(
                        event = action.event,
                        interactionId = interaction.id
                    )
                    if (result !is EngagementResult.Success) {
                        // FIXME: error message
                    }
                }
                else -> {
                    throw IllegalArgumentException("Unexpected action: $action")
                }
            }
        }
        onDismiss?.invoke()
    }

    fun onCancel() {
        context.executors.state.execute {
            engageCodePoint(CODE_POINT_CANCEL)
        }
    }

    private fun engageCodePoint(codePoint: String, data: Map<String, Any?>? = null) {
        context.engage(
            event = Event.internal(codePoint, interaction = "TextModal"),
            interactionId = interaction.id,
            data = data
        )
    }

    private fun indexOfAction(id: String): Int {
        val index = interaction.actions.indexOfFirst { it.id == id }
        if (index == -1) {
            throw IllegalArgumentException("Can't find action: $id")
        }
        return index
    }

    companion object {
        const val CODE_POINT_INTERACTION = "interaction"
        const val CODE_POINT_CANCEL = "cancel"
        const val CODE_POINT_DISMISS = "dismiss"
    }
}