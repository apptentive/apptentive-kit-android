package apptentive.com.android.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import apptentive.com.android.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

class ApptentiveGenericDialog : DialogFragment() {

    data class DialogButton(val title: String, val action: (() -> Unit))

    fun getGenericDialog(
        context: Context,
        title: String?,
        message: String?,
        positiveButton: DialogButton,
        negativeButton: DialogButton? = null
    ): AlertDialog {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        val ctx = ContextThemeWrapper(context, R.style.Theme_Apptentive).apply {
            overrideTheme()
        }

        val dialogLayout = LayoutInflater.from(ctx).inflate(R.layout.apptentive_generic_dialog, null)
        dialogBuilder.setView(dialogLayout)
        val dialog = dialogBuilder.create()

        val titleView = dialogLayout.findViewById<MaterialTextView>(R.id.apptentive_generic_dialog_title)
        titleView.text = title

        val messageView = dialogLayout.findViewById<MaterialTextView>(R.id.apptentive_generic_dialog_message)
        messageView.text = message

        val positiveButtonView = dialogLayout.findViewById<MaterialButton>(R.id.apptentive_generic_dialog_positive)
        positiveButtonView.text = positiveButton.title
        positiveButtonView.setOnClickListener {
            positiveButton.action.invoke()
            dialog.dismiss()
        }

        val negativeButtonView = dialogLayout.findViewById<MaterialButton>(R.id.apptentive_generic_dialog_negative)
        negativeButtonView.isVisible = negativeButton != null
        negativeButton?.let { negButton ->
            negativeButtonView.text = negButton.title
            negativeButtonView.setOnClickListener {
                negButton.action.invoke()
                dialog.dismiss()
            }
        }

        return dialog
    }
}
