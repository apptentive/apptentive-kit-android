package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.interactions.InteractionData

interface InteractionResolver {
    fun getInteraction(event: Event): InteractionData?
}
