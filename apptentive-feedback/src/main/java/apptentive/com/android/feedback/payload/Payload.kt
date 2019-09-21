package apptentive.com.android.feedback.payload

import apptentive.com.android.util.generateUUID

open class Payload(val nonce: String = generateUUID())