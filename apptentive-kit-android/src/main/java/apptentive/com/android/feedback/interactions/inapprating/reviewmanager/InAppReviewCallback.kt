package apptentive.com.android.feedback.interactions.inapprating.reviewmanager

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal interface InAppReviewCallback {
    fun onReviewComplete()
    fun onReviewFlowFailed(message: String)
}
