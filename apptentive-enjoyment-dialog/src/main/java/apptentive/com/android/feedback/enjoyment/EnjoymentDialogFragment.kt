package apptentive.com.android.feedback.enjoyment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class EnjoymentDialogFragment(
    val context: AndroidEngagementContext,
    private val viewModel: EnjoymentDialogViewModel
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
            setMessage(viewModel.title)

            setPositiveButton(viewModel.yesText) { _, _ ->
                viewModel.onYesButton()
            }

            setNegativeButton(viewModel.noText) { _, _ ->
                viewModel.onNoButton()
            }

            setOnCancelListener { viewModel.onCancel() }
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onDismiss()
        super.onDismiss(dialog)
    }
}
