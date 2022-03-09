package apptentive.com.android.feedback.enjoyment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

internal class EnjoymentDialogFragment : DialogFragment() {
    private val viewModel by viewModels<EnjoymentDialogViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val ctx = ContextThemeWrapper(
                requireContext(),
                R.style.Theme_Apptentive
            ).apply {
                overrideTheme()
            }
            val enjoymentDialogView = LayoutInflater.from(ctx).inflate(R.layout.apptentive_enjoyment_dialog, null)
            val messageView = enjoymentDialogView.findViewById<MaterialTextView>(R.id.title)
            messageView.text = viewModel.title

            val positiveButtonView = enjoymentDialogView.findViewById<MaterialButton>(R.id.yes)
            positiveButtonView.text = viewModel.yesText
            positiveButtonView.setOnClickListener {
                viewModel.onYesButton()
                this@EnjoymentDialogFragment.dismiss()
            }

            val negativeButtonView = enjoymentDialogView.findViewById<MaterialButton>(R.id.no)
            negativeButtonView.text = viewModel.noText
            negativeButtonView.setOnClickListener {
                viewModel.onNoButton()
                this@EnjoymentDialogFragment.dismiss()
            }

            setView(enjoymentDialogView)
        }.create()
        return dialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancel()
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onDismiss()
        super.onDismiss(dialog)
    }
}
