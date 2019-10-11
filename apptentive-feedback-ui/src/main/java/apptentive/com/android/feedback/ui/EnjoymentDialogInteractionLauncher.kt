package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// TODO: UI-tests
internal class EnjoymentDialogInteractionLauncher :
    AndroidInteractionLauncher<EnjoymentDialogInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: EnjoymentDialogInteraction
    ) {
        val viewModel = EnjoymentDialogViewModel(context)

        MaterialAlertDialogBuilder(context.androidContext).apply {
            setTitle(interaction.title)
            setPositiveButton(interaction.yesText) { _, _ ->
                viewModel.onYesButton()
            }
            setNegativeButton(interaction.noText) { _, _ ->
                viewModel.onNoButton()
            }
            if (interaction.dismissText != null) {
                setNeutralButton(interaction.dismissText) { _, _ ->
                    viewModel.onDismissButton()
                }
            }
            setOnCancelListener {
                viewModel.onCancel()
            }
            show()
        }
    }
}