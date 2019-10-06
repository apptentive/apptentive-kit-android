package apptentive.com.android.feedback.engagement.interactions

interface InteractionLauncher<in T : Interaction> {
    fun launchInteraction(interaction: T)
}