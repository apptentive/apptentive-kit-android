package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.model.interactions.Interaction

interface InteractionEngagement {
    fun engage(interaction: Interaction): EngagementResult
}
