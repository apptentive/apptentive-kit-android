package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.interactions.InteractionData

// TODO: rename to InteractionResolver or similar
interface InteractionRepository {
    fun getInteraction(event: Event): InteractionData?
}