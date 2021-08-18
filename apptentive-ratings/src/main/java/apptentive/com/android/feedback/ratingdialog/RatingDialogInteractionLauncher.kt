package apptentive.com.android.feedback.ratingdialog

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.util.Log

class RatingDialogInteractionLauncher : AndroidViewInteractionLauncher<RatingDialogInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: RatingDialogInteraction
    ) {
        Log.i(INTERACTIONS, "Rating Dialog interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Rating Dialog interaction data: $interaction")

        val viewModel = RatingDialogViewModel(context, interaction)

        context.executors.main.execute {
            try {
                val fragmentManager = context.getFragmentManager()

                val ratingDialog = RatingDialogFragment(context, viewModel)
                ratingDialog.show(fragmentManager, RatingDialogInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Rating Dialog interaction", exception)
            }
        }
    }
}
