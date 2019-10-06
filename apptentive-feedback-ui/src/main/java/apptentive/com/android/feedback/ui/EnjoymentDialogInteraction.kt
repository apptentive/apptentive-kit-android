package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.Interaction

class EnjoymentDialogInteraction(
    val id: String,
    val title: String,
    val yesText: String,
    val noText: String,
    val dismissText: String?
) : Interaction