package apptentive.com.android.feedback.notes.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TextModalDialog(
    context: Context,
    private val viewModel: TextModalViewModel
) {
    private var dialog: AlertDialog

    init {
        dialog = MaterialAlertDialogBuilder(context).apply {
            val inflater = LayoutInflater.from(context)
            val contentView = inflater.inflate(R.layout.apptentive_note, null)
            setView(contentView)

            val titleView = contentView.findViewById<TextView>(R.id.apptentive_note_title)
            titleView.text = viewModel.title

            val viewGroup = contentView.findViewById<ViewGroup>(R.id.apptentive_note_button_bar)
            viewModel.actions.reversed().forEach { action ->
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
        }.create()

        viewModel.onDismiss = {
            dialog.dismiss()
        }
    }

    @MainThread
    fun show() {
        require(!dialog.isShowing) { "Dialog already showing" }

        dialog.show()
        viewModel.launch()
    }
}