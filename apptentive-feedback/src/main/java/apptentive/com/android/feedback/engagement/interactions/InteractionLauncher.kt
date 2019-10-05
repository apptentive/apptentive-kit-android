package apptentive.com.android.feedback.engagement.interactions

interface InteractionLauncher<T : Interaction> {
    fun launchInteraction(interaction: T)
}