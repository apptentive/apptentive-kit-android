package apptentive.com.android.feedback.payload

class PayloadRejectedException(payload: PayloadData) : PayloadSendException(payload, "payload rejected")
