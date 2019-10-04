package apptentive.com.android.feedback.ui

import android.content.Context
import apptentive.com.android.feedback.model.interactions.InteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EnjoymentDialogInteractionLauncher(private val context: Context) :
    InteractionLauncher<EnjoymentDialogInteraction> {
    override fun launchInteraction(interaction: EnjoymentDialogInteraction) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle(interaction.title)
            setPositiveButton(interaction.yesText) { _, _ ->
            }
            setNegativeButton(interaction.noText) { _, _ ->
            }
            if (interaction.dismissText != null) {
                setNeutralButton(interaction.dismissText) { _, _ ->
                }
            }
            show()
        }
    }
}