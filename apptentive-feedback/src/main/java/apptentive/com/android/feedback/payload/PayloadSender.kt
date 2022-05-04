package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface PayloadSender {
    fun sendPayload(payload: Payload)
}
