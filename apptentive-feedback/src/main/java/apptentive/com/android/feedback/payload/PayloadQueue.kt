package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface PayloadQueue {
    fun enqueuePayload(payload: PayloadData)
    fun nextUnsentPayload(): PayloadData?
    fun deletePayloadAndAssociatedFiles(payload: PayloadData)
    fun updateCredential(credentialProvider: ConversationCredentialProvider, oldTag: String)
    fun invalidateCredential(tag: String)
}
