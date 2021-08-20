package apptentive.com.android.feedback.rating.reviewmanager

interface InAppReviewCallback {
    fun onReviewComplete()
    fun onReviewFlowFailed(message: String)
}
