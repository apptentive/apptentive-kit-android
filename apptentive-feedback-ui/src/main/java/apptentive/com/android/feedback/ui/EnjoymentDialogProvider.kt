package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.InteractionProvider

// TODO: exclude this class from ProGuard
internal class EnjoymentDialogProvider : InteractionProvider<EnjoymentDialogInteraction> {
    override val interactionClass = EnjoymentDialogInteraction::class.java
    override val interactionConverter get() = EnjoymentDialogInteractionConverter()
    override val interactionLauncher get() = EnjoymentDialogInteractionLauncher()
}