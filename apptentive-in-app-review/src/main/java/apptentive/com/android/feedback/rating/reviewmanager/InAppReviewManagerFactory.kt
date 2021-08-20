package apptentive.com.android.feedback.rating.reviewmanager

import android.content.Context

interface InAppReviewManagerFactory {
    fun createReviewManager(context: Context) : InAppReviewManager
}

