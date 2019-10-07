package apptentive.com.android.feedback.engagement.interactions

// TODO: all the subclasses should be excluded from ProGuard
interface InteractionModule<T : Interaction> {
    val interactionClass: Class<T>
    fun provideInteractionConverter(): InteractionConverter<T>
    fun provideInteractionLauncher(): InteractionLauncher<T>
}