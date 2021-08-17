package apptentive.com.android.feedback.enjoyment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.ui.overrideTheme
import apptentive.com.android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EnjoymentDialogFragment(
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
            val interaction = viewModel.interaction

            setMessage(interaction.title)

            setPositiveButton(interaction.yesText) { _, _ ->
                Log.i(INTERACTIONS, "Love dialog positive button pressed")
                viewModel.onYesButton()
            }

            setNegativeButton(interaction.noText) { _, _ ->
                Log.i(INTERACTIONS, "Love dialog negative button pressed")
                viewModel.onNoButton()
            }

            setOnCancelListener {
                Log.i(INTERACTIONS, "Love dialog cancelled")
                viewModel.onCancel()
            }
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.i(INTERACTIONS, "Love dialog dismissed")
        super.onDismiss(dialog)
    }
}
