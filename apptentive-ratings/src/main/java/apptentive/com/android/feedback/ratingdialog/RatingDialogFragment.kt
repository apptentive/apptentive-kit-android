package apptentive.com.android.feedback.ratingdialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.DialogTitle
import androidx.fragment.app.DialogFragment
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.ratings.R
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class RatingDialogFragment(
    val context: AndroidEngagementContext,
    private val viewModel: RatingDialogViewModel
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true

        return MaterialAlertDialogBuilder(context.androidContext).apply {
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