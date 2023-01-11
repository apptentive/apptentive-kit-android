package apptentive.com.android.feedback.textmodal

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

        saveInteractionBackup(interaction, engagementContext.getAppActivity())

        engagementContext.executors.main.execute {
            try {
                val fragmentManager = engagementContext.getFragmentManager()
                val isNoteShowing = fragmentManager.findFragmentByTag(TextModalInteraction.TAG) != null
                require(!isNoteShowing) { "Note already showing" }
                DependencyProvider.register(TextModalInteractionProvider(interaction))

                val noteDialog = TextModalDialogFragment()
                noteDialog.show(fragmentManager, TextModalInteraction.TAG)
            } catch (exception: Exception) {
                Log.e(INTERACTIONS, "Could not start Note interaction", exception)
            }
        }
    }
}
