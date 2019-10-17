package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.interactions.InteractionData

interface InteractionRepository {
    fun getInteraction(event: Event): InteractionData?
}