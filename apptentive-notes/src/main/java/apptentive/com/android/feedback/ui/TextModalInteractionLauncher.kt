package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TextModalInteractionLauncher : AndroidInteractionLauncher<TextModalInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: TextModalInteraction
    ) {
        context.executors.main.execute {
            MaterialAlertDialogBuilder(context.androidContext).apply {
                setTitle(interaction.title)
                setMessage(interaction.body)
                show()
            }
        }
    }
}
