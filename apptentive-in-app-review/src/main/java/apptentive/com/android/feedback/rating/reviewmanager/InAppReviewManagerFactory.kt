package apptentive.com.android.feedback.rating.reviewmanager

import android.content.Context

internal interface InAppReviewManagerFactory {
    fun createReviewManager(context: Context) : InAppReviewManager
}

