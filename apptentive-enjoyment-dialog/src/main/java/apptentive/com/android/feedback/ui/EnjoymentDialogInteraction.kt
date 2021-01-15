package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType

internal class EnjoymentDialogInteraction(
    id: String,
    val title: String,
    val yesText: String,
    val noText: String,
    val dismissText: String?
) : Interaction(id = id, type = InteractionType.EnjoymentDialog)