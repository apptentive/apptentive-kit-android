package apptentive.com.android.feedback.textmodal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.View.TEXT_ALIGNMENT_VIEW_END
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.DialogTitle
import androidx.fragment.app.DialogFragment
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class TextModalDialogFragment(
    val context: AndroidEngagementContext,
    private val viewModel: TextModalViewModel
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true

        val ctx = ContextThemeWrapper(
            context.androidContext,
            R.style.Theme_Apptentive_Dialog_Alert
        ).apply {
            overrideTheme()
        }

        return MaterialAlertDialogBuilder(ctx).apply {
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
            viewModel.actions.forEach { action ->
                val button = inflater.inflate(R.layout.apptentive_note_action, null) as TextView
                button.text = action.title
                button.textAlignment =
                    if (viewModel.actions.size > 2) TEXT_ALIGNMENT_VIEW_END
                    else TEXT_ALIGNMENT_CENTER

                viewGroup.addView(button)
                button.setOnClickListener {
                    action.invoke()
                }
            }

            setOnCancelListener {
                viewModel.onCancel()
            }
        }.create()
    }
}
