package apptentive.com.android.feedback.ratingdialog

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
import apptentive.com.android.feedback.ratings.R
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import apptentive.com.android.R as CoreR

internal class RatingDialogFragment : DialogFragment(), ApptentiveActivityInfo {

    private val viewModel by viewModels<RatingDialogViewModel>()

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!Apptentive.isApptentiveActivityInfoCallbackRegistered()) {
            // Calling this in onCreateDialog in case we lose the Activity reference from the
            // last Activity for whatever reason (garbage collection while app is in the background)
            Apptentive.registerApptentiveActivityInfoCallback(this)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val contextWrapper = ContextThemeWrapper(requireContext(), CoreR.style.Theme_Apptentive).apply {
                overrideTheme()
            }
            val inflater = LayoutInflater.from(contextWrapper)
            val contentView = inflater.inflate(R.layout.apptentive_rating_dialog, null)
            setView(contentView)

            val titleView = contentView.findViewById<MaterialTextView>(R.id.apptentive_rating_dialog_title)
            titleView.text = viewModel.title.orEmpty()

            val messageView = contentView.findViewById<MaterialTextView>(R.id.apptentive_rating_dialog_message)
            messageView.text = viewModel.message.orEmpty()

            val rateButton = contentView.findViewById<MaterialButton>(R.id.apptentive_rating_dialog_button)
            rateButton.text = viewModel.rateText.orEmpty()
            rateButton.setOnClickListener {
                viewModel.onRateButton()
                dismiss()
            }

            val remindButton = contentView.findViewById<MaterialButton>(R.id.apptentive_rating_dialog_remind_button)
            remindButton.text = viewModel.remindText.orEmpty()
            remindButton.setOnClickListener {
                viewModel.onRemindButton()
                dismiss()
            }

            val declineButton = contentView.findViewById<MaterialButton>(R.id.apptentive_rating_dialog_decline_button)
            declineButton.text = viewModel.declineText.orEmpty()
            declineButton.setOnClickListener {
                viewModel.onDeclineButton()
                dismiss()
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

    override fun getApptentiveActivityInfo(): Activity {
        return requireActivity()
    }
}
