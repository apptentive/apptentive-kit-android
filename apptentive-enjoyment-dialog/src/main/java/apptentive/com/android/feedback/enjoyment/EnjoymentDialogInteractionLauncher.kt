package apptentive.com.android.feedback.enjoyment

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// TODO: UI-tests
internal class EnjoymentDialogInteractionLauncher :
    AndroidViewInteractionLauncher<EnjoymentDialogInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: EnjoymentDialogInteraction
    ) {
        val viewModel = EnjoymentDialogViewModel(context, interaction)

        context.executors.main.execute {
            val ctx: Context = ContextThemeWrapper(context.androidContext, R.style.Theme_Apptentive_Dialog_Alert).apply {
                overrideTheme()
            }

            val dialog = MaterialAlertDialogBuilder(ctx).apply {
                val contentView = LayoutInflater.from(ctx)
                    .inflate(R.layout.apptentive_enjoyment_dialog, null)
                setView(contentView)

                val titleView = contentView.findViewById<TextView>(R.id.alertTitle)
                titleView.text = interaction.title

                val yesButton = contentView.findViewById<TextView>(R.id.positiveButton)
                yesButton.text = interaction.yesText
                yesButton.setOnClickListener {
                    viewModel.onYesButton()
                }

                val noButton = contentView.findViewById<TextView>(R.id.negativeButton)
                noButton.text = interaction.noText
                noButton.setOnClickListener {
                    viewModel.onNoButton()
                }

                setOnCancelListener {
                    viewModel.onCancel()
                }
            }.show()

            viewModel.onDismiss = {
                dialog.dismiss()
            }
        }
    }
}