package apptentive.com.android.feedback.engagement.interactions

import android.content.Context

interface InteractionProvider<T : Interaction> {
    fun provideConverter() : InteractionConverter<T>
    fun provideLauncher(context: Context) : InteractionLauncher<T>
}