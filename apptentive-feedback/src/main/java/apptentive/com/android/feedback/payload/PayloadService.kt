package apptentive.com.android.feedback.payload

import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Result

@InternalUseOnly
interface PayloadService {
    fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit)
}
