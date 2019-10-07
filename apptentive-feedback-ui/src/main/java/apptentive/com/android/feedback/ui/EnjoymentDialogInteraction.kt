package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.Interaction

internal class EnjoymentDialogInteraction(
    id: String,
    val title: String,
    val yesText: String,
    val noText: String,
    val dismissText: String?
) : Interaction(id)