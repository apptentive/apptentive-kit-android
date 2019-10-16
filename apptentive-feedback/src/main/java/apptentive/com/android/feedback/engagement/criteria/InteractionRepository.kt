package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionData

interface InteractionRepository {
    fun getInteractionData(event: Event): InteractionData?
}

