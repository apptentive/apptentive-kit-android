package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionId

data class EngagementData(
    val events: EngagementRecords<Event> = EngagementRecords(),
    val interactions: EngagementRecords<InteractionId> = EngagementRecords()
)