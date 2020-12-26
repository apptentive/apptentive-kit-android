package apptentive.com.android.feedback.notes.viewmodel

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction

class TextModalViewModel(
    private val interaction: TextModalInteraction,
    private val executors: Executors,
    private val invocationCallback: (List<InvocationData>) -> Unit
) {
    fun invokeAction(id: String) {
        executors.state.execute {
            val action = findAction(id) ?: throw IllegalArgumentException("Can't find action: $id")
            when (action) {
                is TextModalInteraction.Action.Dismiss -> {
                    // nothing
                }
                is TextModalInteraction.Action.Invoke -> {
                    invocationCallback.invoke(action.invocations)
                }
                else -> {
                    throw IllegalArgumentException("Unexpected action: $action")
                }
            }
        }
    }

    private fun findAction(id: String): TextModalInteraction.Action? {
        return interaction.actions.find { it.id == id }
    }
}