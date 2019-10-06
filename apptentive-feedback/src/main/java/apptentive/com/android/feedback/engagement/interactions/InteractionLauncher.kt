package apptentive.com.android.feedback.engagement.interactions

import android.content.Context

interface InteractionLauncher<in T : Interaction> {
    fun launchInteraction(context: Context, interaction: T)
}