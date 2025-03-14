package apptentive.com.android.feedback.textmodal

import android.content.Intent
import android.os.Handler
import android.os.Looper
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.utils.saveInteractionBackup
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class TextModalInteractionLauncher : AndroidViewInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: TextModalInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)

        Log.i(INTERACTIONS, "Note interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Note interaction data: $interaction")

        saveInteractionBackup(interaction)

        engagementContext.executors.main.execute {
            try {
                DependencyProvider.register(TextModalInteractionProvider(interaction))
                val fragmentManager = engagementContext.getFragmentManager()
                Log.v(INTERACTIONS, "Fragmentmanager obatained from: ${engagementContext.getAppActivity()}")
                val isNoteShowing = fragmentManager.findFragmentByTag(TextModalInteraction.TAG) != null
                require(!isNoteShowing) { "Note already showing" }

                val noteDialog = TextModalDialogFragment()
                noteDialog.show(fragmentManager, TextModalInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Note interaction, launching host activity", exception)
                launchHostActivityWithARetry(engagementContext, interaction, 1)
            }
        }
    }

    private fun launchHostActivityWithARetry(engagementContext: EngagementContext, interaction: TextModalInteraction, retryCount: Int) {
        engagementContext.executors.main.execute {
            try {
                engagementContext.getAppActivity().startActivity(Intent((engagementContext.getAppActivity()), TextModalSupportFragmentManagerActivity::class.java))
            } catch (e: Exception) {
                if (retryCount > 0) {
                    engagementContext.executors.state.execute {
                        Log.i(INTERACTIONS, "Could not start Note interaction using host activity, retrying in 1 second")
                        Handler(Looper.getMainLooper()).postDelayed({
                            launchHostActivityWithARetry(engagementContext, interaction, retryCount - 1)
                        }, 1000)
                    }
                } else {
                    Log.e(INTERACTIONS, "Could not start Note interaction using host activity after a retry", e)
                }
            }
        }
    }
}
