package apptentive.com.android.feedback.payload

import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
interface PayloadQueue {
    fun enqueuePayload(payload: PayloadData)
    fun nextUnsentPayload(): PayloadData?
    fun deletePayload(payload: PayloadData)
}
