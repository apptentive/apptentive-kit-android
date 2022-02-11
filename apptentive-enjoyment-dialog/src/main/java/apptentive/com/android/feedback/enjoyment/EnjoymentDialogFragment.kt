package apptentive.com.android.feedback.enjoyment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class EnjoymentDialogFragment : DialogFragment() {
    private val viewModel by viewModels<EnjoymentDialogViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(viewModel.title)

            setPositiveButton(viewModel.yesText) { _, _ ->
                viewModel.onYesButton()
            }

            setNegativeButton(viewModel.noText) { _, _ ->
                viewModel.onNoButton()
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

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onDismiss()
        super.onDismiss(dialog)
    }
}
