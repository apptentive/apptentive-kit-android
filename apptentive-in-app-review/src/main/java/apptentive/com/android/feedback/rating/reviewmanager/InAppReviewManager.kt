package apptentive.com.android.feedback.rating.reviewmanager

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal interface InAppReviewManager {
    fun startReviewFlow(callback: InAppReviewCallback)

    fun isInAppReviewSupported(): Boolean
}
