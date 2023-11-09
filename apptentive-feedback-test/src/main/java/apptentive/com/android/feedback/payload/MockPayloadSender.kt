package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.model.payloads.Payload

class MockPayloadSender(
    private val callback: ((Payload) -> Unit)? = null,
    var payload: Payload? = null,
    var credentialProvider: ConversationCredentialProvider? = null
) : PayloadSender {
    override fun enqueuePayload(payload: Payload, credentialProvider: ConversationCredentialProvider) {
        this.payload = payload
        this.credentialProvider = credentialProvider
        callback?.invoke(payload)
    }

    override fun updateCredential(credentialProvider: ConversationCredentialProvider) {}
}
