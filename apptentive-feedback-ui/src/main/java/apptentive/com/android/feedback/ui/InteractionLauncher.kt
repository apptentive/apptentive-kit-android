package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.model.interactions.Interaction

interface InteractionLauncher<T : Interaction> {
    fun launchInteraction(interaction: T)
}