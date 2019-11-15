package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result
import java.lang.RuntimeException

interface PayloadService {
    fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit)
}

class NullPayloadService : PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        callback.invoke(Result.Error(RuntimeException("Payload was not sent")))
    }
}

