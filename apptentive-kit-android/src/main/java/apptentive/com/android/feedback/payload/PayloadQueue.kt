package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider

internal interface PayloadQueue {
    fun enqueuePayload(payload: PayloadData)
    fun nextUnsentPayload(): PayloadData?
    fun deletePayloadAndAssociatedFiles(payload: PayloadData)
    fun updateCredential(credentialProvider: ConversationCredentialProvider)
    fun invalidateCredential(tag: String)
}
