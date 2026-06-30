package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval

internal const val DEFAULT_PER_SESSION_INTERACTION_LIMIT = 1
internal const val DEFAULT_SDK_ENABLED = true
/**
 * Data container class for Apptentive configuration. Due to serialization limitation, we have unimplemented hibernateUntil flag
 *
 * @param lastUpdate - Length of time before Status expires and should re-fetch from server
 *
 * @param messageCenter - Configuration for Message Center
 *
 * @param metricsEnabled - Determines if the Apptentive SDK should collect metrics.
 *
 * @param hibernateUntil - not used
 *
 * @param perSessionInteractionLimit - Sets the number of times an interaction can be shown before it is throttled.
 *
 * @param sdkEnabled - Kill switch. When `false`, all network requests (except the status endpoint) are
 * blocked and no interactions will be shown. Defaults to `true`.
 *
 * If `true`, the SDK will collect metrics and send them to the Apptentive API.
 *
 * If `false`, the SDK will collect metrics to user internally. It won't send them to the Apptentive API.
 **/

internal data class SDKStatus(
    val expiry: TimeInterval = 0.0,
    val messageCenter: MessageCenter = MessageCenter(),
    val lastUpdate: TimeInterval = 0.0,
    val metricsEnabled: Boolean = true,
    val hibernateUntil: TimeInterval? = null,
    val perSessionInteractionLimit: Int = DEFAULT_PER_SESSION_INTERACTION_LIMIT,
    val sdkEnabled: Boolean = DEFAULT_SDK_ENABLED,
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
