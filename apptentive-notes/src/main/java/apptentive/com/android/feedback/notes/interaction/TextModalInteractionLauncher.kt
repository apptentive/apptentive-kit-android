package apptentive.com.android.feedback.notes.interaction

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TextModalInteractionLauncher : AndroidInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        val viewModel = TextModalViewModel(
            context = context,
            interaction = interaction
        )
        context.executors.main.execute {
            val dialog = MaterialAlertDialogBuilder(context.androidContext).apply {
                val inflater = LayoutInflater.from(context.androidContext)
                val contentView = inflater.inflate(R.layout.apptentive_note, null)
                setView(contentView)

                val titleView = contentView.findViewById<TextView>(R.id.alertTitle)
                titleView.text = viewModel.title

                val viewGroup = contentView.findViewById<ViewGroup>(R.id.apptentive_note_button_bar)
                viewModel.actions.forEach { action ->
                    val button = inflater.inflate(R.layout.apptentive_note_action, null) as TextView
                    button.text = action.title
                    viewGroup.addView(button)
                    button.setOnClickListener {
                        action.invoke()
                    }
                }

                setOnCancelListener {
                    viewModel.cancel()
                }

                viewModel.launch()
            }.show()

            viewModel.onDismiss = {
                dialog.dismiss()
            }
        }
    }
}
