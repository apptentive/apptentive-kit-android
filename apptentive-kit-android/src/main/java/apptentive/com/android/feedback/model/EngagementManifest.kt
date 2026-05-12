package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.util.InternalUseOnly
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import java.net.URL

/**
 * Data container class for raw representation of Interaction Data and Targets.
 */
@InternalUseOnly
data class EngagementManifest(
    val applicationId: String = "",
    val interactions: List<InteractionData> = emptyList(),
    val targets: Map<String, List<InvocationData>> = emptyMap(),
    val expiry: TimeInterval = 0.0,
    val prefetch: List<URL> = emptyList(),
)
