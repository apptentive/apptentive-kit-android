package apptentive.com.android.feedback.enjoyment

import android.content.Intent
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.utils.saveInteractionBackup
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class EnjoymentDialogInteractionLauncher :
    AndroidViewInteractionLauncher<EnjoymentDialogInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: EnjoymentDialogInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)
        Log.i(INTERACTIONS, "Love Dialog interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Love Dialog interaction data: $interaction")

        engagementContext.executors.main.execute {
            try {
                saveInteractionBackup(interaction)
                DependencyProvider.register(EnjoymentDialogInteractionProvider(interaction))

                val fragmentManager = engagementContext.getFragmentManager()
                val enjoymentDialog = EnjoymentDialogFragment()
                enjoymentDialog.show(fragmentManager, EnjoymentDialogInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Love Dialog interaction, launching host activity", exception)
                try {
                    engagementContext.getAppActivity().startActivity(
                        Intent(
                            (engagementContext.getAppActivity()),
                            EnjoymentDialogSupportFragmentManagerActivity::class.java
                        )
                    )
                } catch (e: Exception) {
                    Log.e(INTERACTIONS, "Could not start Love Dialog interaction using host activity", e)
                }
            }
        }
    }
}
