package apptentive.com.android.feedback.ratingdialog

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.utils.saveInteractionBackup

internal class RatingDialogInteractionLauncher : AndroidViewInteractionLauncher<RatingDialogInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: RatingDialogInteraction,
        whereEvent: String?,
    ) {
        super.launchInteraction(engagementContext, interaction, whereEvent)
        Log.i(INTERACTIONS, "Rating Dialog interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Rating Dialog interaction data: $interaction")

        engagementContext.executors.main.execute {
            try {
                saveInteractionBackup(interaction)
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
