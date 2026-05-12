package apptentive.com.android.feedback.inapprating.reviewmanager

import android.content.Context

internal interface InAppReviewManagerFactory {
    fun createReviewManager(context: Context): InAppReviewManager
}
