package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.feedback.notes.view.TextModalDialog
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher

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
            TextModalDialog(context.androidContext, viewModel).show()
        }
    }
}
