package apptentive.com.android.feedback.ui

import android.content.Context
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class EnjoymentDialogInteractionLauncher :
    InteractionLauncher<EnjoymentDialogInteraction> {
    override fun launchInteraction(context: Context, interaction: EnjoymentDialogInteraction) {
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