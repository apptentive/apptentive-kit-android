package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.util.Log

// TODO: UI-tests
internal class EnjoymentDialogInteractionLauncher :
    AndroidViewInteractionLauncher<EnjoymentDialogInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: EnjoymentDialogInteraction
    ) {
        Log.i(INTERACTIONS, "Love Dialog interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Love Dialog interaction data: $interaction")

        context.executors.main.execute {
            try {
                val fragmentManager = context.getFragmentManager()
                val viewModel = EnjoymentDialogViewModel(context, interaction)

                val enjoymentDialog = EnjoymentDialogFragment(context, viewModel)
                enjoymentDialog.show(fragmentManager, EnjoymentDialogInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Love Dialog interaction", exception)
            }
        }
    }
}
