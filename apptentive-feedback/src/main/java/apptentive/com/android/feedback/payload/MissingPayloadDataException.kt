package apptentive.com.android.feedback.payload

class MissingPayloadDataException(nonce: String) : Exception("Missing payload data: $nonce")