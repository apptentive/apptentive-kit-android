package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.model.EngagementRecord

internal data class InteractionResponseData(
    val responses: Set<InteractionResponse> = setOf(),
    val currentResponses: Set<InteractionResponse> = setOf(),
    val record: EngagementRecord = EngagementRecord()
)
