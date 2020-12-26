package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TextModalInteractionLauncher : AndroidInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        val viewModel = TextModalViewModel(interaction)
        context.executors.main.execute {
            MaterialAlertDialogBuilder(context.androidContext).apply {
                setTitle(interaction.title)
                setMessage(interaction.body)
                val actions = interaction.actions
                setPositiveButton(actions[0].label) { _, _ ->
                    viewModel.invokeAction(actions[0].id)
                }
                if (actions.size > 1) {
                    setNegativeButton(actions[1].label) { _, _ ->
                        viewModel.invokeAction(actions[1].id)
                    }
                }
                if (actions.size > 2) {
                    setNeutralButton(actions[2].label) { _, _ ->
                        viewModel.invokeAction(actions[2].id)
                    }
                }
                show()
            }
        }
    }
}
