package apptentive.com.android.feedback.textmodal

import androidx.lifecycle.ViewModel
import apptentive.com.android.core.Callback
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.engagement.AndroidEngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.util.Log

internal class TextModalViewModel : ViewModel()
{
    val interaction = DependencyProvider.of<TextModalInteractionFactory>().getTextModalInteraction()
    val context = DependencyProvider.of<AndroidEngagementContextFactory>().engagementContext()
    val title = interaction.title
    val message = interaction.body
    val actions = interaction.actions.mapIndexed { index, action ->
        ActionModel(
            title = action.label,
            callback = {
                // invoke action
                context.executors.state.execute(createActionCallback(action, index))

                // dismiss UI
                onDismiss?.invoke()
            }
        )
    }

    var onDismiss: Callback? = null

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

    private fun createActionCallback(action: TextModalInteraction.Action, index: Int): Callback =
        when (action) {
            is TextModalInteraction.Action.Dismiss -> {
                {
                    Log.i(INTERACTIONS, "Note dismissed")
                    // engage event
                    val data = createEventData(action, index)
                    engageCodePoint(CODE_POINT_DISMISS, data)
                }
            }
            is TextModalInteraction.Action.Invoke -> {
                {
                    Log.i(INTERACTIONS, "Note action invoked")

                    // run invocation
                    val result = context.engage(action.invocations)

                    // engage event
                    val data = createEventData(action, index, result)
                    engageCodePoint(CODE_POINT_INTERACTION, data)
                }
            }
            is TextModalInteraction.Action.Event -> {
                {
                    Log.i(INTERACTIONS, "Note event engaged")

                    // engage target event
                    val result = context.engage(
                        event = action.event,
                        interactionId = interaction.id
                    )

                    // engage event
                    val data = createEventData(action, index, result)
                    engageCodePoint(CODE_POINT_EVENT, data)
                }
            }
            else -> {
                throw IllegalArgumentException("Unexpected action: $action")
            }
        }

    data class ActionModel(val title: String, val callback: Callback) {
        operator fun invoke() {
            callback.invoke()
        }
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
                val interactionId = (engagementResult as? EngagementResult.InteractionShown)?.interactionId
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