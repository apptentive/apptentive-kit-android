package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.engagement.interactions.InteractionData

// TODO: exclude this class from ProGuard
data class EngagementManifest(
    val interactions: List<InteractionData> = emptyList(),
    val targets: Map<String, List<InvocationData>> = emptyMap(),
    val expiry: TimeInterval = 0.0
)