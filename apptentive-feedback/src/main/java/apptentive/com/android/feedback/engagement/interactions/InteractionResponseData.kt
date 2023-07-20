package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class InteractionResponseData(
    val responses: Set<InteractionResponse> = setOf(),
    val currentResponses: Set<InteractionResponse> = setOf(),
    val record: EngagementRecord = EngagementRecord()
)
