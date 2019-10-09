package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event

data class EnjoymentDialogViewModel(private val context: EngagementContext) {
    fun onYesButton() {
        engageCodePoint(CODE_POINT_YES)
    }

    fun onNoButton() {
        engageCodePoint(CODE_POINT_NO)
    }

    fun onDismissButton() {
        engageCodePoint(CODE_POINT_DISMISS)
    }

    fun onCancel() {
        engageCodePoint(CODE_POINT_CANCEL)
    }

    private fun engageCodePoint(name: String) {
        context.engage(Event.internal(name))
    }

    companion object {
        internal const val CODE_POINT_DISMISS = "dismiss"
        internal const val CODE_POINT_CANCEL = "cancel"
        internal const val CODE_POINT_YES = "yes"
        internal const val CODE_POINT_NO = "no"
    }
}