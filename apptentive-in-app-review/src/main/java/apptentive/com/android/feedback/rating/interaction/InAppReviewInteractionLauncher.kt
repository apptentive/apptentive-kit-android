package apptentive.com.android.feedback.rating.interaction

import apptentive.com.android.feedback.IN_APP_REVIEW
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewCallback
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewManagerFactory
import apptentive.com.android.util.Log

private const val DATA_KEY_CAUSE = "cause"

internal class InAppReviewInteractionLauncher(private val inAppReviewManagerFactory: InAppReviewManagerFactory) : AndroidViewInteractionLauncher<InAppReviewInteraction>() {

    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: InAppReviewInteraction
    ) {
        context.engage(Event.internal(InternalEvent.EVENT_REQUEST.labelName, interaction.type), interaction.id)

        val reviewManager = inAppReviewManagerFactory.createReviewManager(context.androidContext)
        if (reviewManager.isInAppReviewSupported()) {
            reviewManager.startReviewFlow(object : InAppReviewCallback {
                override fun onReviewComplete() {
                    onReviewShown(context, interaction)
                }

                override fun onReviewFlowFailed(message: String) {
                    onReviewNotShown(context, interaction, message)
                }
            })
        } else {
            onReviewNotSupported(context, interaction)
        }
    }

    fun onReviewShown(engagementContext: AndroidEngagementContext, interaction: InAppReviewInteraction) {
        engagementContext.engage(Event.internal(InternalEvent.EVENT_SHOWN.labelName, interaction.type), interaction.id)
        Log.i(IN_APP_REVIEW, "InAppReview is shown")
    }

    fun onReviewNotShown(engagementContext: AndroidEngagementContext, interaction: InAppReviewInteraction, message: String) {
        val data = mapOf(DATA_KEY_CAUSE to message)
        engagementContext.engage(Event.internal(InternalEvent.EVENT_NOT_SHOWN.labelName, interaction.type), interaction.id, data)
        Log.d(IN_APP_REVIEW, "InAppReview is not shown")
    }

    private fun onReviewNotSupported(engagementContext: AndroidEngagementContext, interaction: InAppReviewInteraction) {
        engagementContext.engage(Event.internal(InternalEvent.EVENT_NOT_SUPPORTED.labelName, interaction.type), interaction.id)
        Log.i(IN_APP_REVIEW, "InAppReview is not supported, no fallback interaction")
    }
}
