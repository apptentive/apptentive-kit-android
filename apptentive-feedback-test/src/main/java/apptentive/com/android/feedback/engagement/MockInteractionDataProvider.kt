package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.mockEvent
import apptentive.com.android.feedback.engagement.interactions.mockInteractionData

class MockInteractionDataProvider : InteractionDataProvider {
    override fun getInteractionData(event: Event): InteractionData? {
        if (event == mockEvent) {
            return mockInteractionData
        }

        return null
    }
}