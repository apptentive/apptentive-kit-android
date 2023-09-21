package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.payloads.Payload

class MockPayloadSender(
    private val callback: ((Payload) -> Unit)? = null,
    var payload: Payload? = null
) : PayloadSender {
    override fun enqueuePayload(payload: Payload) {
        this.payload = payload
        callback?.invoke(payload)
    }
}
