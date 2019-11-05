package apptentive.com.android.feedback.engagement.interactions

interface InteractionTypeConverter<out T : Interaction> {
    fun convert(data: InteractionData): T
}