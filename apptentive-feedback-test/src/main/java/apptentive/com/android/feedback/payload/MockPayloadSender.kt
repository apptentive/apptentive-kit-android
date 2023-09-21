package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.payloads.Payload

class MockPayloadSender(
    private val callback: ((Payload) -> Unit)? = null,
    var payload: Payload? = null,
    var payloadContext: PayloadContext? = null
) : PayloadSender {
    override fun enqueuePayload(payload: Payload, context: PayloadContext) {
        this.payload = payload
        this.payloadContext = context
        callback?.invoke(payload)
    }
}
