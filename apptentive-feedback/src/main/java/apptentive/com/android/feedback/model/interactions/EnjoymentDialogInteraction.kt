package apptentive.com.android.feedback.model.interactions

class EnjoymentDialogInteraction(
    id: String,
    val title: String,
    val yesText: String,
    val noText: String,
    val dismissText: String?
) : Interaction(id)