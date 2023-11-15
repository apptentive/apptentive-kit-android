package apptentive.com.android.feedback.enjoyment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import apptentive.com.android.R as CoreR

internal class EnjoymentDialogFragment : DialogFragment(), ApptentiveActivityInfo {
    private val viewModel by viewModels<EnjoymentDialogViewModel>()

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!Apptentive.isApptentiveActivityInfoCallbackRegistered()) {
            // Calling this in onCreateDialog in case we lose the Activity reference from the
            // last Activity for whatever reason (garbage collection while app is in the background)
            Apptentive.registerApptentiveActivityInfoCallback(this)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val ctx = ContextThemeWrapper(
                requireContext(),
                CoreR.style.Theme_Apptentive
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

    override fun getApptentiveActivityInfo(): Activity {
        return requireActivity()
    }
}
