package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.engagement.interactions.InteractionData

/**
 * Data container class for raw representation of Interaction Data and Targets.
 */
data class EngagementManifest(
    val interactions: List<InteractionData> = emptyList(),
    val targets: Map<String, List<InvocationData>> = emptyMap(),
    val expiry: TimeInterval = 0.0
)
