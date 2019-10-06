package apptentive.com.android.feedback.engagement.interactions

interface InteractionConverter<T : Interaction> {
    fun convert(data: InteractionData): T
}