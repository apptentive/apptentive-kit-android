package apptentive.com.android.feedback.rating.interaction

import android.os.Handler
import android.os.Looper
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewCallback
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewManagerFactory
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.IN_APP_REVIEW

private const val DATA_KEY_CAUSE = "cause"

internal class InAppReviewInteractionLauncher(private val inAppReviewManagerFactory: InAppReviewManagerFactory) :
    AndroidViewInteractionLauncher<InAppReviewInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: InAppReviewInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)
        engagementContext.engage(Event.internal(InternalEvent.EVENT_REQUEST.labelName, interaction.type), interaction.id)
        createReviewManagerWithARetry(engagementContext, interaction, 1)
    }

    private fun createReviewManagerWithARetry(engagementContext: EngagementContext, interaction: InAppReviewInteraction, retry: Int) {
        try {
            val reviewManager = inAppReviewManagerFactory.createReviewManager(engagementContext.getAppActivity())
            if (reviewManager.isInAppReviewSupported()) {
                reviewManager.startReviewFlow(object : InAppReviewCallback {
                    override fun onReviewComplete() {
                        onReviewShown(engagementContext, interaction)
                    }

                    override fun onReviewFlowFailed(message: String) {
                        onReviewNotShown(engagementContext, interaction, message)
                    }
                })
            } else {
                onReviewNotSupported(engagementContext, interaction)
            }
        } catch (e: Exception) {
            if (retry > 0) {
                engagementContext.executors.state.execute {
                    Log.i(IN_APP_REVIEW, "Could not start InAppReview interaction, retrying in 1 second")
                    Handler(Looper.getMainLooper()).postDelayed({
                        createReviewManagerWithARetry(engagementContext, interaction, retry - 1)
                    }, 1000)
                }
            } else
                Log.e(IN_APP_REVIEW, "Could not start InAppReview interaction after a retry", e)
        }
    }

    fun onReviewShown(engagementContext: EngagementContext, interaction: InAppReviewInteraction) {
        engagementContext.engage(Event.internal(InternalEvent.EVENT_SHOWN.labelName, interaction.type), interaction.id)
        Log.i(IN_APP_REVIEW, "InAppReview is shown")
    }

    fun onReviewNotShown(engagementContext: EngagementContext, interaction: InAppReviewInteraction, message: String) {
        val data = mapOf(DATA_KEY_CAUSE to message)
        engagementContext.engage(Event.internal(InternalEvent.EVENT_NOT_SHOWN.labelName, interaction.type), interaction.id, data)
        Log.d(IN_APP_REVIEW, "InAppReview is not shown")
    }

    private fun onReviewNotSupported(engagementContext: EngagementContext, interaction: InAppReviewInteraction) {
        engagementContext.engage(Event.internal(InternalEvent.EVENT_NOT_SUPPORTED.labelName, interaction.type), interaction.id)
        Log.i(IN_APP_REVIEW, "InAppReview is not supported, no fallback interaction")
    }
}
