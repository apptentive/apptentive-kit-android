package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class EnjoymentDialogInteractionLauncher :
    AndroidInteractionLauncher<EnjoymentDialogInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: EnjoymentDialogInteraction
    ) {
        // FIXME: add UI configuration and view model
        MaterialAlertDialogBuilder(context.androidContext).apply {
            setTitle(interaction.title)
            setPositiveButton(interaction.yesText) { _, _ ->
                context.engage(Event.internal(CODE_POINT_YES))
            }
            setNegativeButton(interaction.noText) { _, _ ->
                context.engage(Event.internal(CODE_POINT_NO))
            }
            if (interaction.dismissText != null) {
                setNeutralButton(interaction.dismissText) { _, _ ->
                    context.engage(Event.internal(CODE_POINT_DISMISS))
                }
            }
            setOnCancelListener {
                context.engage(Event.internal(CODE_POINT_CANCEL))
            }
            show()
        }
    }

    companion object {
        private const val CODE_POINT_DISMISS = "dismiss"
        private const val CODE_POINT_CANCEL = "cancel"
        private const val CODE_POINT_YES = "yes"
        private const val CODE_POINT_NO = "no"
    }
}