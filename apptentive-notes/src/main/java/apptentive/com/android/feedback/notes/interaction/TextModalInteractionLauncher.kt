package apptentive.com.android.feedback.notes.interaction

import androidx.appcompat.view.ContextThemeWrapper
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.feedback.notes.view.TextModalDialog
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import apptentive.com.android.ui.overrideTheme

class TextModalInteractionLauncher : AndroidInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        val viewModel = TextModalViewModel(
            context = context,
            interaction = interaction
        )
        context.executors.main.execute {
            val ctx = ContextThemeWrapper(context.androidContext, R.style.Theme_Apptentive_Dialog_Alert).apply {
                overrideTheme()
            }
            TextModalDialog(ctx, viewModel).show()
        }
    }
}
