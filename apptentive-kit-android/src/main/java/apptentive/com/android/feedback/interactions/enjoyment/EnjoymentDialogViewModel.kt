package apptentive.com.android.feedback.interactions.enjoyment

import androidx.lifecycle.ViewModel
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LogTags.INTERACTIONS
import apptentive.com.android.core.platform.AndroidSharedPrefDataStore
import apptentive.com.android.core.platform.SharedPrefConstants.FAN_SIGNAL_TIME_STAMP
import apptentive.com.android.core.platform.SharedPrefConstants.SDK_CORE_INFO
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getEngagementContextOrNull
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.feedback.utils.getWhereEventBackup
import apptentive.com.android.ui.core.LiveEvent
import apptentive.com.android.ui.toGravity
import apptentive.com.android.util.Log
import apptentive.com.android.util.getTimeSeconds

internal class EnjoymentDialogViewModel : ViewModel() {
    val dismissInteraction: LiveEvent<Unit> = LiveEvent()
    private val context = getEngagementContextOrNull() ?: run {
        dismissInteraction.postValue(Unit)
        null
    }

    private val model: EnjoymentDialogModel? = try {
        DependencyProvider.of<EnjoymentDialogInteractionFactory>().getEnjoymentDialogInteraction()
    } catch (e: Exception) {
        getEnjoymentDialogModelBackup(e) ?: run {
            dismissInteraction.postValue(Unit)
            null
        }
    }

    val interaction = model?.interaction

    val title = interaction?.title
    val yesText = interaction?.yesText
    val noText = interaction?.noText
    val verticalMargins = interaction?.verticalMargins

    val sharedPrefDataStore = DependencyProvider.of<AndroidSharedPrefDataStore>()

    fun getEnjoymentDialogPosition(): Int? = interaction?.position?.toGravity()

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
        engageCodePoint(CODE_POINT_CANCEL, null)
    }

    private fun engageCodePoint(name: String, whereEvent: String? = model?.whereEvent) {
        // capture the time stamp of LD response
        if (name == CODE_POINT_NO || name == CODE_POINT_YES) {
            sharedPrefDataStore.putString(
                SDK_CORE_INFO,
                FAN_SIGNAL_TIME_STAMP,
                getTimeSeconds().toString()
            )
        }
        context?.executors?.state?.execute {
            interaction?.let {
                context.engage(
                    event = Event.internal(name, it.type),
                    interactionId = it.id,
                    whereEvent = whereEvent,
                )
            }
        }
    }

    private fun getEnjoymentDialogModelBackup(e: Exception): EnjoymentDialogModel? {
        Log.e(
            INTERACTIONS,
            "EnjoymentDialogInteractionFactory is not registered, trying to build EnjoymentDialogModel from backup...",
            e
        )
        return try {
            EnjoymentDialogModel(getInteractionBackup(), getWhereEventBackup())
        } catch (e: Exception) {
            Log.e(INTERACTIONS, "Building EnjoymentDialogModel from backup failed", e)
            null
        }
    }

    companion object {
        internal const val CODE_POINT_DISMISS = "dismiss"
        internal const val CODE_POINT_CANCEL = "cancel"
        internal const val CODE_POINT_YES = "yes"
        internal const val CODE_POINT_NO = "no"
    }
}
