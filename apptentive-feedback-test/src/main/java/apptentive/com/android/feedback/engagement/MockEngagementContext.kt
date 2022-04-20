package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.MockPayloadSender

data class EngageArgs(
    val event: Event,
    val interactionId: String? = null,
    val data: Map<String, Any?>? = null,
    val customData: Map<String, Any?>? = null,
    val extendedData: List<ExtendedData>? = null
)

typealias EngagementCallback = (EngageArgs) -> EngagementResult

typealias InvocationCallback = (List<Invocation>) -> EngagementResult

typealias PayloadSenderCallback = (Payload) -> Unit

class MockEngagementContext(
    onEngage: EngagementCallback? = null,
    onInvoke: InvocationCallback? = null,
    onSendPayload: PayloadSenderCallback? = null
) : EngagementContext(
    engagement = object : Engagement {
        override fun engage(
            context: EngagementContext,
            event: Event,
            interactionId: String?,
            data: Map<String, Any?>?,
            customData: Map<String, Any?>?,
            extendedData: List<ExtendedData>?,
            interactionResponses: Map<String, Set<InteractionResponse>>?
        ): EngagementResult {
            return onEngage?.invoke(
                EngageArgs(
                    event,
                    interactionId,
                    data,
                    customData,
                    extendedData
                )
            ) ?: EngagementResult.InteractionNotShown("No runnable interactions")
        }

        override fun engage(
            context: EngagementContext,
            invocations: List<Invocation>
        ): EngagementResult {
            return onInvoke?.invoke(invocations) ?: EngagementResult.InteractionNotShown("No runnable interactions")
        }
    },
    payloadSender = MockPayloadSender(onSendPayload),
    executors = Executors(ImmediateExecutor, ImmediateExecutor)
)
