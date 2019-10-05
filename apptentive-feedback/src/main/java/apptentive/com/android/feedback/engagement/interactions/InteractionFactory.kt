package apptentive.com.android.feedback.engagement.interactions

interface InteractionFactory {
    fun createInteraction(data: InteractionData): Interaction?
}