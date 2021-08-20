package apptentive.com.android.feedback.rating.reviewmanager

interface InAppReviewManager {
    fun startReviewFlow(callback: InAppReviewCallback)

    fun isInAppReviewSupported(): Boolean
}
