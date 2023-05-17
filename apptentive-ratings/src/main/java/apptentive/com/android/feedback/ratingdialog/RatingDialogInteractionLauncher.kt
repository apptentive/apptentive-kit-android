package apptentive.com.android.feedback.ratingdialog

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.utils.saveInteractionBackup
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class RatingDialogInteractionLauncher : AndroidViewInteractionLauncher<RatingDialogInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: RatingDialogInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)
        Log.i(INTERACTIONS, "Rating Dialog interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Rating Dialog interaction data: $interaction")

        engagementContext.executors.main.execute {
            try {
                saveInteractionBackup(interaction, engagementContext.getAppActivity())
                val fragmentManager = engagementContext.getFragmentManager()
                DependencyProvider.register(RatingDialogInteractionProvider(interaction))
                val ratingDialog = RatingDialogFragment()
                ratingDialog.show(fragmentManager, RatingDialogInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Rating Dialog interaction", exception)
            }
        }
    }
}
