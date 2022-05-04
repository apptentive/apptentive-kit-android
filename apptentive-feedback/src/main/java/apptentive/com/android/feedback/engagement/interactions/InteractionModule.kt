package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

/* NOTE: all the subclasses should be excluded from ProGuard */
@InternalUseOnly
interface InteractionModule<T : Interaction> {
    val interactionClass: Class<T>
    fun provideInteractionTypeConverter(): InteractionTypeConverter<T>
    fun provideInteractionLauncher(): InteractionLauncher<T>
}
