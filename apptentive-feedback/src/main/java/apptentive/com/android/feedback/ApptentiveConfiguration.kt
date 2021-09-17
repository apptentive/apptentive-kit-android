package apptentive.com.android.feedback

import apptentive.com.android.util.LogLevel
import java.util.concurrent.TimeUnit

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
     * LogLevel is used to define what level of logs we will show in Logcat
     *
     * @see LogLevel.Verbose - Any relevant info not shown in other log Levels
     * @see LogLevel.Debug   - Processes with more technical information
     * @see LogLevel.Info    - General processes and non-technical results
     * @see LogLevel.Warning - Non-breaking / handled issues
     * @see LogLevel.Error   - Breaking / unhandled issues (Throwables)
     * @see Throwable
     */
    var logLevel: LogLevel = LogLevel.Info

    /**
     * Redacts sensitive information from being logged when set to true
     *
     * Data fields which have @SensitiveDataKey annotation will replace the logged data with
     * @see Constants.REDACTED_DATA
     */
    var shouldSanitizeLogMessages: Boolean = true

    /**
     * A time based throttle which determines when a rating interaction can be shown again.
     * This is a safeguard on top of the criteria already set in the Apptentive Dashboard.
     * This applies to both Google In-App Review & Apptentive Rating Dialog interactions.
     *
     * @see TimeUnit for conversion utils
     * e.g. TimeUnit.DAYS.toMillis(30)
     */
    var ratingInteractionThrottleLength = TimeUnit.DAYS.toMillis(7)

    init {
        require(apptentiveKey.isNotEmpty()) { "apptentiveKey is null or empty" }
        require(apptentiveSignature.isNotEmpty()) { "apptentiveSignature is null or empty" }
    }
}
