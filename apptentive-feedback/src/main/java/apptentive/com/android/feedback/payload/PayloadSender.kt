package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface PayloadSender {
    fun enqueuePayload(payload: Payload, credentialProvider: ConversationCredentialProvider)
    fun updateCredential(credentialProvider: ConversationCredentialProvider)
}
