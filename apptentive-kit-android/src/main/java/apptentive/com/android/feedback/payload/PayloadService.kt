package apptentive.com.android.feedback.payload

import apptentive.com.android.core.util.InternalUseOnly
import apptentive.com.android.core.util.Result

@InternalUseOnly
interface PayloadService {
    fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit)
}
