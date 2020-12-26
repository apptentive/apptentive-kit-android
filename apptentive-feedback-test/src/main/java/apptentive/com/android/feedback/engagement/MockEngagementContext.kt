package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.MockPayloadSender

data class EngageArgs(
    val event: Event,
    val interactionId: String? = null,
    val data: Map<String, Any>? = null,
    val customData: Map<String, Any>? = null,
    val extendedData: List<ExtendedData>? = null
)

typealias EngagementCallback = (EngageArgs) -> EngagementResult

typealias PayloadSenderCallback = (Payload) -> Unit

class MockEngagementContext(
    onEngage: EngagementCallback? = null,
    onSendPayload: PayloadSenderCallback? = null
) : EngagementContext(
    engagement = object : Engagement {
        override fun engage(
            context: EngagementContext,
            event: Event,
            interactionId: String?,
            data: Map<String, Any>?,
            customData: Map<String, Any>?,
            extendedData: List<ExtendedData>?
        ): EngagementResult {
            return onEngage?.invoke(
                EngageArgs(
                    event,
                    interactionId,
                    data,
                    customData,
                    extendedData
                )
            ) ?: EngagementResult.Success
        }

        override fun engage(
            context: EngagementContext,
            invocations: List<Invocation>
        ): EngagementResult {
            TODO("Not yet implemented")
        }
    },
    payloadSender = MockPayloadSender(onSendPayload),
    executors = Executors(ImmediateExecutor, ImmediateExecutor)
)