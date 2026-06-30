package apptentive.com.android.feedback.interactions.inapprating.reviewmanager

import android.content.Context

internal interface InAppReviewManagerFactory {
    fun createReviewManager(context: Context): InAppReviewManager
}
