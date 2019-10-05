package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction

interface InteractionEngagement {
    fun engage(interaction: Interaction): EngagementResult
}
