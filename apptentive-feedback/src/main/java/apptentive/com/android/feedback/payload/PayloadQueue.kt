package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.conversation.ConversationCredential
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface PayloadQueue {
    fun enqueuePayload(payload: PayloadData)
    fun nextUnsentPayload(): PayloadData?
    fun deletePayloadAndAssociatedFiles(payload: PayloadData)
    fun invalidateToken(tag: String)
    fun updateCredential(credential: ConversationCredential, oldTag: String)
}
