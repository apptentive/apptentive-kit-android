package apptentive.com.android.feedback.payload

class PayloadRejectedException(payload: Payload) : PayloadSendException(payload, "payload rejected")
