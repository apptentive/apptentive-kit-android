package apptentive.com.android.feedback.payload

internal class PayloadRejectedException(payload: PayloadData) : PayloadSendException(payload, "payload rejected")
