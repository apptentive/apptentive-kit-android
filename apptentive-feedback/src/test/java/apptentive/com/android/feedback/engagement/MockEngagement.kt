package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.model.payloads.ExtendedData

class MockEngagement(private val callback: ((context: EngagementContext, event: Event) -> EngagementResult)? = null) : Engagement {
    override fun engage(
        context: EngagementContext,
        event: Event,
        interactionId: String?,
        data: Map<String, Any>?,
        customData: Map<String, Any>?,
        extendedData: List<ExtendedData>?
    ): EngagementResult {
        return callback?.invoke(context, event) ?: EngagementResult.Success
    }
}