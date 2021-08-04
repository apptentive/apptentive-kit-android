package apptentive.com.android.feedback.enjoyment

import androidx.appcompat.view.ContextThemeWrapper
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.ui.overrideTheme
import apptentive.com.android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// TODO: UI-tests
internal class EnjoymentDialogInteractionLauncher :
    AndroidViewInteractionLauncher<EnjoymentDialogInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: EnjoymentDialogInteraction
    ) {
        Log.i(INTERACTIONS, "Love Dialog interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Love Dialog interaction data: $interaction")
        val viewModel = EnjoymentDialogViewModel(context, interaction)

        context.executors.main.execute {
            val ctx = ContextThemeWrapper(context.androidContext, R.style.Theme_Apptentive_Dialog_Alert).apply {
                overrideTheme()
            }

            val dialog = MaterialAlertDialogBuilder(ctx).apply {
                setMessage(interaction.title)

                setPositiveButton(interaction.yesText) { _, _ ->
                    Log.i(INTERACTIONS, "Love dialog positive button pressed")
                    viewModel.onYesButton()
                }

                setNegativeButton(interaction.noText) { _, _ ->
                    Log.i(INTERACTIONS, "Love dialog negative button pressed")
                    viewModel.onNoButton()
                }

                setOnCancelListener {
                    Log.i(INTERACTIONS, "Love dialog cancelled")
                    viewModel.onCancel()
                }
            }.show()

            viewModel.onDismiss = {
                Log.i(INTERACTIONS, "Love dialog dismissed")
                dialog.dismiss()
            }
        }
    }
}
