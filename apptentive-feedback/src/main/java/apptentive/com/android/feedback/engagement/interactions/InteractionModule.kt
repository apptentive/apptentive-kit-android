package apptentive.com.android.feedback.engagement.interactions

/* NOTE: all the subclasses should be excluded from ProGuard */
interface InteractionModule<T : Interaction> {
    val interactionClass: Class<T>
    fun provideInteractionTypeConverter(): InteractionTypeConverter<T>
    fun provideInteractionLauncher(): InteractionLauncher<T>
}