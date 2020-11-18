package apptentive.com.android.feedback

import apptentive.com.android.util.LogLevel

data class ApptentiveConfiguration(
    val apptentiveKey: String,
    val apptentiveSignature: String,
    val logLevel: LogLevel = LogLevel.Info
)