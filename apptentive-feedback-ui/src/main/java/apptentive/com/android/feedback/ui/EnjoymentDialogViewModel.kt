package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event

// TODO: unit tests
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
        private const val CODE_POINT_DISMISS = "dismiss"
        private const val CODE_POINT_CANCEL = "cancel"
        private const val CODE_POINT_YES = "yes"
        private const val CODE_POINT_NO = "no"
    }
}