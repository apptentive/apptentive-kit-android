package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.MockPayloadSender
import apptentive.com.android.feedback.payload.PayloadSender

class MockEngagementContext(
    onEngage: ((Event) -> EngagementResult)? = null,
    onSendPayload: ((Payload) -> Unit)? = null
) :
    EngagementContext(
        engagement = object : Engagement {
            override fun engage(
                context: EngagementContext,
                event: Event,
                interactionId: String?,
                data: Map<String, Any>?,
                customData: Map<String, Any>?,
                extendedData: List<ExtendedData>?
            ): EngagementResult {
                return onEngage?.invoke(event) ?: EngagementResult.Success
            }
        },
        payloadSender = MockPayloadSender(onSendPayload),
        executors = Executors(ImmediateExecutor, ImmediateExecutor)
    )