package apptentive.com.android.feedback.engagement.interactions

interface InteractionConverter<out T : Interaction> {
    fun convert(data: InteractionData): T
}