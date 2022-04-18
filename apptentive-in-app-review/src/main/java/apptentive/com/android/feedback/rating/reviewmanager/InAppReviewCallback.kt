package apptentive.com.android.feedback.rating.reviewmanager

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal interface InAppReviewCallback {
    fun onReviewComplete()
    fun onReviewFlowFailed(message: String)
}
