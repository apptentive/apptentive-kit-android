package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.model.payloads.ExtendedData

class MockEngagementContext(onEngage: ((Event) -> EngagementResult)? = null) :
    EngagementContext(
        engagement = object : Engagement {
            override fun engage(
                context: EngagementContext,
                event: Event,
                interactionId: String?,
                data: Map<String, Any>?,
                customData: Map<String, Any>?,
                vararg extendedData: ExtendedData
            ): EngagementResult {
                return onEngage?.invoke(event) ?: EngagementResult.Success
            }
        },
        executors = Executors(ImmediateExecutor, ImmediateExecutor)
    )