package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.engagement.interactions.InteractionResponseData
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class EngagementData(
    val events: EngagementRecords<Event> = EngagementRecords(),
    val interactions: EngagementRecords<InteractionId> = EngagementRecords(),
    val interactionResponses: MutableMap<InteractionId, InteractionResponseData> = mutableMapOf(),
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

    fun addInvoke(
        interactionId: InteractionId,
        responses: Set<InteractionResponse>,
        versionName: VersionName,
        versionCode: VersionCode,
        lastInvoked: DateTime
    ) = copy(
        interactionResponses = interactionResponses.apply {
            val recordedInteraction = get(interactionId)

            put(
                interactionId,
                InteractionResponseData(
                    responses = responses.union(recordedInteraction?.responses.orEmpty()),
                    currentResponses = setOf(),
                    record = (recordedInteraction?.record ?: EngagementRecord()).addInvoke(
                        versionName = versionName,
                        versionCode = versionCode,
                        lastInvoked = lastInvoked
                    )
                )
            )
        }
    )

    fun updateCurrentAnswer(
        interactionId: InteractionId,
        responses: Set<InteractionResponse>,
        versionName: VersionName,
        versionCode: VersionCode,
        lastInvoked: DateTime,
        reset: Boolean
    ) = copy(
        interactionResponses = interactionResponses.apply {
            val recordedInteraction = get(interactionId)
            put(
                interactionId,
                InteractionResponseData(
                    responses = recordedInteraction?.responses.orEmpty(),
                    currentResponses = if (reset) setOf() else responses,
                    record = (recordedInteraction?.record ?: EngagementRecord()).addInvoke(
                        versionName = versionName,
                        versionCode = versionCode,
                        lastInvoked = lastInvoked
                    )
                )
            )
        }
    )
}
