package apptentive.com.android.feedback.interactions.inapprating.reviewmanager

internal class UnSupportedReviewManager : InAppReviewManager {
    override fun startReviewFlow(callback: InAppReviewCallback) {
        callback.onReviewFlowFailed("In-app review is not supported")
    }

    override fun isInAppReviewSupported(): Boolean = false
}
