package apptentive.com.android.feedback.engagement.interactions

interface InteractionProvider<T : Interaction> {
    val interactionClass : Class<T>
    val interactionConverter: InteractionConverter<T>
    val interactionLauncher: InteractionLauncher<T>
}