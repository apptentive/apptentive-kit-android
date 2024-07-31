package apptentive.com.android.feedback.enjoyment

import androidx.lifecycle.ViewModel
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.core.MissingProviderException
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class EnjoymentDialogViewModel : ViewModel() {
    val dismissInteraction: LiveEvent<Unit> = LiveEvent()
    private val context: EngagementContext? = try {
        DependencyProvider.of<EngagementContextFactory>().engagementContext()
    } catch (exception: MissingProviderException) {
        dismissInteraction.postValue(Unit)
        Log.e(INTERACTIONS, "EngagementContextFactory is not registered, cannot launch EnjoymentDialogViewModel", exception)
        null
    }

    private val interaction: EnjoymentDialogInteraction = try {
        DependencyProvider.of<EnjoymentDialogInteractionFactory>().getEnjoymentDialogInteraction()
    } catch (exception: Exception) {
        getInteractionBackup()
    }

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
        context?.executors?.state?.execute {
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
