package apptentive.com.android.feedback.notes.viewmodel

import apptentive.com.android.core.Callback
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction

class TextModalViewModel(
    private val context: EngagementContext,
    private val interaction: TextModalInteraction
) {
    var onDismiss: Callback? = null

    fun invokeAction(id: String) {
        context.executors.state.execute {
            val action = findAction(id) ?: throw IllegalArgumentException("Can't find action: $id")
            when (action) {
                is TextModalInteraction.Action.Dismiss -> {
                    // nothing
                }
                is TextModalInteraction.Action.Invoke -> {
                    val result = context.engage(action.invocations)
                    if (result != EngagementResult.Success) {
                        // FIXME: error message
                    }
                }
                is TextModalInteraction.Action.Event -> {
                    val result = context.engage(
                        event = action.event,
                        interactionId = interaction.id
                    )
                    if (result != EngagementResult.Success) {
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
        // TODO: engage 'cancel' event?
    }

    private fun findAction(id: String): TextModalInteraction.Action? {
        return interaction.actions.find { it.id == id }
    }
}