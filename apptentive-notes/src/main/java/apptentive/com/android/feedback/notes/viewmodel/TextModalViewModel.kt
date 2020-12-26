package apptentive.com.android.feedback.notes.viewmodel

import apptentive.com.android.feedback.notes.interaction.TextModalInteraction

class TextModalViewModel(private val interaction: TextModalInteraction) {
    fun invokeAction(id: String) {
        val action = findAction(id) ?: throw IllegalArgumentException("Can't find action: $id")
        when (action) {
            is TextModalInteraction.Action.Dismiss -> {
                // nothing
            }
            is TextModalInteraction.Action.Invoke -> {
                TODO()
            }
            else -> {
                throw IllegalArgumentException("Unexpected action: $action")
            }
        }
    }

    private fun findAction(id: String): TextModalInteraction.Action? {
        return interaction.actions.find { it.id == id }
    }
}