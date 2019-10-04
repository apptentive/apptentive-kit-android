package apptentive.com.android.feedback.model.interactions

interface InteractionLauncher<T : Interaction> {
    fun launchInteraction(interaction: T)
}