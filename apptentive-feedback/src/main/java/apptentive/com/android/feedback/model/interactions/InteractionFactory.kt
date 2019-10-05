package apptentive.com.android.feedback.model.interactions

interface InteractionFactory {
    fun createInteraction(data: InteractionData): Interaction?
}