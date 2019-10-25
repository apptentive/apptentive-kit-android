package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.engagement.Event

val mockEvent = Event.local("mock")
val mockInteractionData = InteractionData(id = "interaction_id", type = "interaction_type")
val mockInteraction = object : Interaction(id = "interaction_id") {}

class MockInteractionFactory : InteractionFactory {
    override fun createInteraction(data: InteractionData): Interaction? {
        if (data == mockInteractionData) {
            return mockInteraction
        }

        return null
    }
}