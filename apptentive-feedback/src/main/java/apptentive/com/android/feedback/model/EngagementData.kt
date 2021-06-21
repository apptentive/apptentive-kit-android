package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class EngagementData(
    val events: EngagementRecords<Event> = EngagementRecords(),
    val interactions: EngagementRecords<InteractionId> = EngagementRecords(),
    val versionHistory: VersionHistory = VersionHistory()
) {
    fun addInvoke(
        event: Event,
        versionName: VersionName,
        versionCode: VersionCode,
        lastInvoked: DateTime
    ) = copy(
        events = events.apply {
            addInvoke(
                key = event,
                versionName = versionName,
                versionCode = versionCode,
                lastInvoked = lastInvoked
            )
        }
    )

    fun addInvoke(
        interactionId: InteractionId,
        versionName: VersionName,
        versionCode: VersionCode,
        lastInvoked: DateTime
    ) = copy(
        interactions = interactions.apply {
            addInvoke(
                key = interactionId,
                versionName = versionName,
                versionCode = versionCode,
                lastInvoked = lastInvoked
            )
        }
    )
}
