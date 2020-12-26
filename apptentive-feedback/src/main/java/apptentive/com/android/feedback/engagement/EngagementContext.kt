package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender

/**
 * Wrapper class around [Engagement] object.
 * Allows capturing platform specific context (e.g. [android.content.Context]) before making an
 * actual engagement call.
 */
open class EngagementContext(
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    val executors: Executors
) {
    fun engage(
        event: Event,
        interactionId: String? = null,
        data: Map<String, Any>? = null,
        customData: Map<String, Any>? = null,
        extendedData: List<ExtendedData>? = null
    ) = engagement.engage(
        context = this,
        event = event,
        interactionId = interactionId,
        data = data,
        customData = customData,
        extendedData = extendedData
    )

    fun engage(invocations: List<InvocationData>) = engagement.engage(
        context = this,
        invocations = invocations.map(InvocationConverter::convert)
    )

    fun sendPayload(payload: Payload) = payloadSender.sendPayload(payload)
}