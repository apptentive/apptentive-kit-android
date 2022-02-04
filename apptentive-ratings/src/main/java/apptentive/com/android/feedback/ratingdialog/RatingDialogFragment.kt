package apptentive.com.android.feedback.ratingdialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.DialogTitle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.feedback.ratings.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class RatingDialogFragment : DialogFragment() {

    private val viewModel by viewModels<RatingDialogViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            val inflater = LayoutInflater.from(context)
            val contentView = inflater.inflate(R.layout.apptentive_rating_dialog, null)
            setView(contentView)

            val titleView = contentView.findViewById<DialogTitle>(R.id.apptentive_rate_title)
            titleView.text = viewModel.title.orEmpty()

            val messageView = contentView.findViewById<TextView>(R.id.apptentive_rate_message)
            messageView.text = viewModel.message.orEmpty()

            val rateButton = contentView.findViewById<TextView>(R.id.apptentive_rate_button)
            rateButton.text = viewModel.rateText.orEmpty()
            rateButton.setOnClickListener {
                viewModel.onRateButton()
                dismiss()
            }

            val remindButton = contentView.findViewById<TextView>(R.id.apptentive_remind_button)
            remindButton.text = viewModel.remindText.orEmpty()
            remindButton.setOnClickListener {
                viewModel.onRemindButton()
                dismiss()
            }

            val declineButton = contentView.findViewById<TextView>(R.id.apptentive_decline_button)
            declineButton.text = viewModel.declineText.orEmpty()
            declineButton.setOnClickListener {
                viewModel.onDeclineButton()
                dismiss()
            }

            setOnCancelListener {
                viewModel.onCancel()
            }
        }.create()
    }
}