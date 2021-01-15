package apptentive.com.android.feedback.enjoyment

import android.view.LayoutInflater
import android.widget.TextView
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
        val viewModel = EnjoymentDialogViewModel(context, interaction)

        context.executors.main.execute {
            val dialog = MaterialAlertDialogBuilder(context.androidContext).apply {
                val contentView = LayoutInflater.from(context.androidContext)
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