package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.model.payloads.Payload

class MockPayloadSender(
    private val callback: ((Payload) -> Unit)? = null,
    var payload: Payload? = null,
    var credentialsProvider: ConversationCredentialProvider? = null
) : PayloadSender {
    override fun enqueuePayload(payload: Payload, credentialsProvider: ConversationCredentialProvider) {
        this.payload = payload
        this.credentialsProvider = credentialsProvider
        callback?.invoke(payload)
    }
}
