package apptentive.com.android.feedback.interactions.ratingdialog

import androidx.lifecycle.ViewModel
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LogTags.INTERACTIONS
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getEngagementContextOrNull
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.ui.core.LiveEvent
import apptentive.com.android.util.Log

internal class RatingDialogViewModel : ViewModel() {
    val dismissInteraction = LiveEvent<Unit>()
    private val context = getEngagementContextOrNull() ?: run {
        dismissInteraction.postValue(Unit)
        null
    }
    private val interaction: RatingDialogInteraction? = try {
        DependencyProvider.of<RatingDialogInteractionFactory>().getRatingDialogInteraction()
    } catch (e: Exception) {
        getInteractionBackup(e) ?: run {
            dismissInteraction.postValue(Unit)
            null
        }
    }

    val title = interaction?.title
    val message = interaction?.body
    val rateText = interaction?.rateText
    val remindText = interaction?.remindText
    val declineText = interaction?.declineText

    fun onRateButton() {
        Log.i(INTERACTIONS, "Rating Dialog rate button pressed")
        engageCodePoint(CODE_POINT_RATE)
    }

    fun onRemindButton() {
        Log.i(INTERACTIONS, "Rating Dialog remind button pressed")
        engageCodePoint(CODE_POINT_REMIND)
    }

    fun onDeclineButton() {
        Log.i(INTERACTIONS, "Rating Dialog decline button pressed")
        engageCodePoint(CODE_POINT_DECLINE)
    }

    fun onCancel() {
        Log.i(INTERACTIONS, "Rating Dialog cancelled")
        engageCodePoint(CODE_POINT_CANCEL)
    }

    private fun engageCodePoint(name: String) {
        context?.executors?.state?.execute {
            interaction?.let {
                context.engage(
                    event = Event.internal(name, it.type),
                    interactionId = it.id
                )
            }
        }
    }

    private fun getInteractionBackup(e: Exception): RatingDialogInteraction? {
        Log.e(
            INTERACTIONS,
            "RatingDialogInteractionFactory is not registered, trying to build RatingDialogInteraction from backup...",
            e
        )
        return try {
            getInteractionBackup()
        } catch (e: Exception) {
            Log.e(INTERACTIONS, "Building RatingDialogInteraction from backup failed", e)
            null
        }
    }

    companion object {
        const val CODE_POINT_RATE = "rate"
        const val CODE_POINT_REMIND = "remind"
        const val CODE_POINT_DECLINE = "decline"
        const val CODE_POINT_DISMISS = "dismiss"
        const val CODE_POINT_CANCEL = "cancel"
    }
}
