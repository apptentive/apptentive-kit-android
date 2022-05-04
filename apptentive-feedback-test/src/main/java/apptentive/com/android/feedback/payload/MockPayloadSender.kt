package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.payloads.Payload

class MockPayloadSender(
    private val callback: ((Payload) -> Unit)? = null
) : PayloadSender {
    override fun sendPayload(payload: Payload) {
        callback?.invoke(payload)
    }
}
