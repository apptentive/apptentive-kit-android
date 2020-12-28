package apptentive.com.android.feedback.notes.interaction

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TextModalInteractionLauncher : AndroidInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        val viewModel = TextModalViewModel(
            interaction = interaction,
            executors = context.executors,
            invocationCallback = { invocations ->
                val result = context.engage(invocations)
                if (result !is EngagementResult.Success) {
                    Log.e(LogTags.core, "No runnable interactions") // TODO: better error message
                }
            },
            eventCallback = { event ->
                val result = context.engage(event)
                if (result !is EngagementResult.Success) {
                    Log.e(LogTags.core, "No runnable interactions for event: $event") // TODO: better error message
                }
            }
        )
        context.executors.main.execute {
            val dialog = MaterialAlertDialogBuilder(context.androidContext).apply {
                val inflater = LayoutInflater.from(context.androidContext)
                val contentView = inflater.inflate(R.layout.apptentive_note, null)
                setView(contentView)

                val titleView = contentView.findViewById<TextView>(R.id.alertTitle)
                titleView.text = interaction.title

                val viewGroup = contentView.findViewById<ViewGroup>(R.id.apptentive_note_button_bar)
                interaction.actions.forEach { action ->
                    val button = inflater.inflate(R.layout.apptentive_note_action, null) as TextView
                    button.text = action.label
                    viewGroup.addView(button)
                    button.setOnClickListener {
                        viewModel.invokeAction(action.id)
                    }
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
