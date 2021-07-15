package apptentive.com.android.feedback.notes.interaction

import androidx.appcompat.view.ContextThemeWrapper
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.feedback.notes.view.TextModalDialog
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.ui.overrideTheme
import apptentive.com.android.util.Log

class TextModalInteractionLauncher : AndroidViewInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        Log.i(INTERACTIONS, "Note interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Note interaction data: $interaction")

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
