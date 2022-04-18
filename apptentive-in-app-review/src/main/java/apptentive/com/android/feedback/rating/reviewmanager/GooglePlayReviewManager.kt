package apptentive.com.android.feedback.rating.reviewmanager

import android.app.Activity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.IN_APP_REVIEW
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task

internal class GooglePlayReviewManager(private val activity: Activity) : InAppReviewManager {
    private val reviewManager = ReviewManagerFactory.create(activity)

    override fun isInAppReviewSupported(): Boolean = true

    override fun startReviewFlow(callback: InAppReviewCallback) {
        val startTime = System.currentTimeMillis()

        Log.d(IN_APP_REVIEW, "Requesting in-app review...")

        val task: Task<ReviewInfo> = reviewManager.requestReviewFlow()
        task.addOnSuccessListener { reviewInfo ->
            val elapsedTime = System.currentTimeMillis() - startTime
            try {
                Log.d(
                    IN_APP_REVIEW,
                    "ReviewInfo request complete (took $elapsedTime ms). Launching startReviewFlow..."
                )
                startReview(activity, reviewInfo, callback)
            } catch (exception: Exception) {
                notifyFailure(
                    callback,
                    exception,
                    "Failure occurred in startReview call (took $elapsedTime ms)"
                )
            }
        }

        task.addOnFailureListener { exception ->
            val elapsedTime = System.currentTimeMillis() - startTime
            notifyFailure(callback, exception, "ReviewInfo request failed (took $elapsedTime ms).")
        }
    }

    private fun notifyFailure(
        callback: InAppReviewCallback,
        exception: Exception?,
        message: String
    ) {
        Log.e(IN_APP_REVIEW, message, exception)
        callback.onReviewFlowFailed(message)
    }

    private fun startReview(
        activity: Activity,
        reviewInfo: ReviewInfo,
        callback: InAppReviewCallback
    ) {
        val startTime = System.currentTimeMillis()
        val task = reviewManager.launchReviewFlow(activity, reviewInfo)
        task.addOnSuccessListener {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 1000L) {
                notifyFailure(
                    callback,
                    null,
                    "In-app review flow completed too fast ($elapsedTime ms) and we have good reasons to believe it just failed silently."
                )
            } else {
                Log.d(IN_APP_REVIEW, "In-app review complete (took $elapsedTime ms)")
                callback.onReviewComplete()
            }
        }

        task.addOnFailureListener { exception ->
            val elapsedTime = System.currentTimeMillis() - startTime
            notifyFailure(
                callback,
                exception,
                "Unable to launch in-app review (took $elapsedTime ms)"
            )
        }
    }
}
