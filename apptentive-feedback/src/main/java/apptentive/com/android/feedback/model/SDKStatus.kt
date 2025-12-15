package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly

/**
 * Data container class for Apptentive configuration.
 *
 * @param lastUpdated - Length of time before Configuration expires and should re-fetch from server
 */
@InternalUseOnly
data class SDKStatus(
    val expiry: TimeInterval = 0.0,
    val messageCenter: MessageCenter = MessageCenter(),
    val lastUpdate: TimeInterval = 0.0,
    val metricsEnabled: Boolean = true,
    val hibernateUntil: TimeInterval? = null,
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
