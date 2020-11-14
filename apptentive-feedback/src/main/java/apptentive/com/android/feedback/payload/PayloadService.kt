package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

interface PayloadService {
    fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit)
}

