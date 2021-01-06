package apptentive.com.android.feedback.notes.viewmodel

import apptentive.com.android.core.Callback
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction
import apptentive.com.android.util.Log
import apptentive.com.android.feedback.INTERACTIONS

class TextModalViewModel(
    private val context: EngagementContext,
    private val interaction: TextModalInteraction
) {
    var onClose: Callback? = null

    fun invokeAction(id: String) {
        context.executors.state.execute {
            val position = indexOfAction(id)
            when (val action = interaction.actions[position]) {
                is TextModalInteraction.Action.Dismiss -> {
                    // engage event
                    val data = createEventData(action, position)
                    engageCodePoint(CODE_POINT_DISMISS, data)
                }
                is TextModalInteraction.Action.Invoke -> {
                    // run invocation
                    val result = context.engage(action.invocations)
                    if (result !is EngagementResult.Success) {
                        Log.e(INTERACTIONS, "No runnable interactions") // TODO: better message
                    }

                    // engage event
                    val data = createEventData(action, position, result)
                    engageCodePoint(CODE_POINT_INTERACTION, data)

                }
                is TextModalInteraction.Action.Event -> {
                    // engage target event
                    val result = context.engage(
                        event = action.event,
                        interactionId = interaction.id
                    )
                    if (result !is EngagementResult.Success) {
                        Log.e(INTERACTIONS, "No runnable interactions") // TODO: better message
                    }

                    // engage event
                    val data = createEventData(action, position, result)
                    engageCodePoint(CODE_POINT_EVENT, data)
                }
                else -> {
                    throw IllegalArgumentException("Unexpected action: $action")
                }
            }
        }
        onClose?.invoke()
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
        const val CODE_POINT_EVENT = "event"
        const val CODE_POINT_DISMISS = "dismiss"
        const val CODE_POINT_CANCEL = "cancel"

        private const val DATA_ACTION_ID = "action_id"
        private const val DATA_ACTION_LABEL = "label"
        private const val DATA_ACTION_POSITION = "position"
        private const val DATA_ACTION_INTERACTION_ID = "invoked_interaction_id"

        private fun createEventData(
            action: TextModalInteraction.Action,
            actionPosition: Int,
            engagementResult: EngagementResult? = null
        ): Map<String, Any?> {
            // we need to include a target interaction id (if any)
            if (engagementResult != null) {
                val interactionId = (engagementResult as? EngagementResult.Success)?.interactionId
                return mapOf(
                    DATA_ACTION_ID to action.id,
                    DATA_ACTION_LABEL to action.label,
                    DATA_ACTION_POSITION to actionPosition,
                    DATA_ACTION_INTERACTION_ID to interactionId
                )
            }

            return mapOf(
                DATA_ACTION_ID to action.id,
                DATA_ACTION_LABEL to action.label,
                DATA_ACTION_POSITION to actionPosition
            )
        }
    }
}