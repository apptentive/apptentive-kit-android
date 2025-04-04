package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly

/**
 * Data container class for Apptentive configuration.
 *
 * @param expiry - Length of time before Configuration expires and should re-fetch from server
 *
 * @param messageCenter - Configuration for Message Center
 *
 * @param metricsEnabled - Determines if the Apptentive SDK should collect metrics.
 *
 *  If `true`, the SDK will collect metrics and send them to the Apptentive API.
 *
 *  If `false`, the SDK will collect metrics to user internally. It won't send them to the Apptentive API.
 */
@InternalUseOnly
data class Configuration(
    var expiry: TimeInterval = 0.0,
    val messageCenter: MessageCenter = MessageCenter(),
    val metricsEnabled: Boolean = true,
) {
    /**
     * Data container class for Apptentive Message Center configuration.
     *
     * @param fgPoll - aka foregroundPollingInterval. Length of time to poll in foreground for an active Message Center conversation
     * @param bgPoll - aka backgroundPollingInterval. Length of time to poll in background for an active Message Center conversation
     */
    data class MessageCenter(
        val fgPoll: TimeInterval = 30.0, // Default 30 seconds
        val bgPoll: TimeInterval = 300.0, // Default 300 seconds (5 minutes)
    )
}
