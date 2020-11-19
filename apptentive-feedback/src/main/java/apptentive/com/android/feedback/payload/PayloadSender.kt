package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.payloads.Payload

interface PayloadSender {
    fun sendPayload(payload: Payload)
}