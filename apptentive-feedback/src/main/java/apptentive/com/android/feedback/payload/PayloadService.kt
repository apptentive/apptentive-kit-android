package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

interface PayloadService {
    fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit)
}

