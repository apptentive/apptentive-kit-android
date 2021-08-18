package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.util.Log

data class EnjoymentDialogViewModel(
    private val context: EngagementContext,
    val interaction: EnjoymentDialogInteraction
) {
    val title = interaction.title
    val yesText = interaction.yesText
    val noText = interaction.noText

    fun onYesButton() {
        Log.i(INTERACTIONS, "Love Dialog positive button pressed")
        engageCodePoint(CODE_POINT_YES)
    }

    fun onNoButton() {
        Log.i(INTERACTIONS, "Love Dialog negative button pressed")
        engageCodePoint(CODE_POINT_NO)
    }

    fun onDismiss() {
        Log.i(INTERACTIONS, "Love Dialog dismissed")
        engageCodePoint(CODE_POINT_DISMISS)
    }

    fun onCancel() {
        Log.i(INTERACTIONS, "Love Dialog cancelled")
        engageCodePoint(CODE_POINT_CANCEL)
    }

    private fun engageCodePoint(name: String) {
        context.executors.state.execute {
            context.engage(
                event = Event.internal(name, interaction.type),
                interactionId = interaction.id
            )
        }
    }

    companion object {
        internal const val CODE_POINT_DISMISS = "dismiss"
        internal const val CODE_POINT_CANCEL = "cancel"
        internal const val CODE_POINT_YES = "yes"
        internal const val CODE_POINT_NO = "no"
    }
}
