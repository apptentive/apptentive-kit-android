package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.mockEvent
import apptentive.com.android.feedback.engagement.interactions.mockInteractionData

class MockInteractionRepository : InteractionRepository {
    override fun getInteraction(event: Event): InteractionData? {
        if (event == mockEvent) {
            return mockInteractionData
        }

        return null
    }
}