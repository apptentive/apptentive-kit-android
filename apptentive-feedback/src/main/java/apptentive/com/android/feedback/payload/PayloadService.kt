package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

interface PayloadService {
    fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit)
}

class NullPayloadService : PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        callback.invoke(Result.Error(PayloadSendException(payload, "Payload was not sent")))
    }
}

