package apptentive.com.android.feedback.enjoyment

import android.annotation.SuppressLint
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

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val ctx = ContextThemeWrapper(
                requireContext(),
                R.style.Theme_Apptentive
            ).apply {
                overrideTheme()
            }
            val enjoymentDialogView = LayoutInflater.from(ctx).inflate(R.layout.apptentive_enjoyment_dialog, null)
            val messageView = enjoymentDialogView.findViewById<MaterialTextView>(R.id.apptentive_enjoyment_dialog_title)
            messageView.text = viewModel.title

            // No -> Yes Orientation (default)
            val positiveButtonView = enjoymentDialogView.findViewById<MaterialButton>(R.id.apptentive_enjoyment_dialog_yes)
            positiveButtonView.text = viewModel.yesText
            positiveButtonView.setOnClickListener {
                viewModel.onYesButton()
                this@EnjoymentDialogFragment.dismiss()
            }

            val negativeButtonView = enjoymentDialogView.findViewById<MaterialButton>(R.id.apptentive_enjoyment_dialog_no)
            negativeButtonView.text = viewModel.noText
            negativeButtonView.setOnClickListener {
                viewModel.onNoButton()
                this@EnjoymentDialogFragment.dismiss()
            }

            // Yes -> No Orientation (alternate - enabled through styles)
            val positiveButtonViewAlternate = enjoymentDialogView.findViewById<MaterialButton>(R.id.apptentive_enjoyment_dialog_yes_alternate)
            positiveButtonViewAlternate.text = viewModel.yesText
            positiveButtonViewAlternate.setOnClickListener {
                viewModel.onYesButton()
                this@EnjoymentDialogFragment.dismiss()
            }

            val negativeButtonViewAlternate = enjoymentDialogView.findViewById<MaterialButton>(R.id.apptentive_enjoyment_dialog_no_alternate)
            negativeButtonViewAlternate.text = viewModel.noText
            negativeButtonViewAlternate.setOnClickListener {
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
