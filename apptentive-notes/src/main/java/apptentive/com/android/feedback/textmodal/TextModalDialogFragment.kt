package apptentive.com.android.feedback.textmodal

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.View.TEXT_ALIGNMENT_VIEW_END
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.DialogTitle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.feedback.notes.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class TextModalDialogFragment : DialogFragment() {

    private val viewModel by viewModels<TextModalViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val inflater = LayoutInflater.from(context)
            val contentView = inflater.inflate(R.layout.apptentive_note, null)
            setView(contentView)

            val titleView = contentView.findViewById<DialogTitle>(R.id.apptentive_note_title)
            if (viewModel.title != null && viewModel.message != null) titleView.text = viewModel.title
            else titleView.visibility = View.GONE

            val messageView = contentView.findViewById<TextView>(R.id.apptentive_note_message)
            when {
                viewModel.message != null -> messageView.text = viewModel.message
                viewModel.title != null -> messageView.text = viewModel.title
                else -> messageView.visibility = View.GONE
            }

            val viewGroup = contentView.findViewById<ViewGroup>(R.id.apptentive_note_button_bar)
            viewModel.onDismiss = { this@TextModalDialogFragment.dismiss()}
            viewModel.actions.forEach { action ->
                val button = inflater.inflate(R.layout.apptentive_note_action, null) as TextView
                button.text = action.title
                button.textAlignment =
                    if (viewModel.actions.size > 1) TEXT_ALIGNMENT_VIEW_END
                    else TEXT_ALIGNMENT_CENTER

                viewGroup.addView(button)
                button.setOnClickListener {
                    action.invoke()
                }
            }
        }.create()
        return dialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancel()
        super.onCancel(dialog)
    }
}

