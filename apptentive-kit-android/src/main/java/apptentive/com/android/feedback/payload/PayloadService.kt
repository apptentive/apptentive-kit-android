package apptentive.com.android.feedback.payload

import apptentive.com.android.core.network.Result

internal interface PayloadService {
    fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit)
}
