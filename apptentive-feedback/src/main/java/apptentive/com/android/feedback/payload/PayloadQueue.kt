package apptentive.com.android.feedback.payload

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface PayloadQueue {
    fun enqueuePayload(payload: PayloadData)
    fun nextUnsentPayload(): PayloadData?
    fun deletePayloadAndAssociatedFiles(payload: PayloadData)
}
