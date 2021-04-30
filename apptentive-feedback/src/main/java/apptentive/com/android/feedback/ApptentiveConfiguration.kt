package apptentive.com.android.feedback

import apptentive.com.android.util.LogLevel

/**
 * This class creates a new ApptentiveConfiguration object which is used to initialize the
 * Apptentive SDK.
 * @param apptentiveKey, The Apptentive Key that should be used when connecting to
 * the Apptentive API.
 * @param apptentiveSignature The Apptentive Signature that should be used when connecting to
 * the Apptentive API.
 */
data class ApptentiveConfiguration(
    val apptentiveKey: String,
    val apptentiveSignature: String
) {
    /**
     * Sets logLevel, default is set to Info.
     */
    var logLevel: LogLevel = LogLevel.Info

    init {
        require(apptentiveKey.isNotEmpty()) { "apptentiveKey is null or empty" }
        require(apptentiveSignature.isNotEmpty()) { "apptentiveSignature is null or empty" }
    }
}