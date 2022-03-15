package apptentive.com.android.feedback.textmodal

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

internal class TextModalDialogFragment : DialogFragment() {

    private val viewModel by viewModels<TextModalViewModel>()

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val contextWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Apptentive).apply {
                overrideTheme()
            }
            val inflater = LayoutInflater.from(contextWrapper)
            val contentView = inflater.inflate(R.layout.apptentive_note, null)
            setView(contentView)

            val noteLayout = contentView.findViewById<LinearLayout>(R.id.apptentive_note_layout)

            //region Title
            val titleView = inflater.inflate(
                if (viewModel.message != null) R.layout.apptentive_note_title_with_message
                else R.layout.apptentive_note_title_no_message, null
            ) as MaterialTextView
            titleView.text = viewModel.title
            noteLayout.addView(titleView)
            //endregion

            //region Message
            if (viewModel.message != null) {
                val messageView = inflater.inflate(R.layout.apptentive_note_message, null) as MaterialTextView
                messageView.text = viewModel.message
                noteLayout.addView(messageView)
            }
            //endregion

            //region Actions
            val buttonLayout = inflater.inflate(R.layout.apptentive_note_actions, null) as LinearLayout
            noteLayout.addView(buttonLayout)

            viewModel.actions.forEach { action ->
                val button = inflater.inflate(R.layout.apptentive_note_action, null) as MaterialButton

                button.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    gravity = Gravity.END
                }
                button.text = action.title

                buttonLayout.addView(button)
                button.setOnClickListener {
                    action.invoke()
                }
            }
            //endregion

            viewModel.onDismiss = { this@TextModalDialogFragment.dismiss() }
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

