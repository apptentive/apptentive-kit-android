package apptentive.com.android.feedback.textmodal

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.util.Log

internal class TextModalInteractionLauncher : AndroidViewInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        Log.i(INTERACTIONS, "Note interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Note interaction data: $interaction")


        context.executors.main.execute {
            try {
                val fragmentManager = context.getFragmentManager()
                val isNoteShowing = fragmentManager.findFragmentByTag(TextModalInteraction.TAG) != null
                require(!isNoteShowing) { "Note already showing" }

                val viewModel = TextModalViewModel(context, interaction)

                val noteDialog = TextModalDialogFragment(context, viewModel)
                viewModel.onDismiss = { noteDialog.dismiss() }

                noteDialog.show(fragmentManager, TextModalInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Note interaction", exception)
            }
        }
    }
}
