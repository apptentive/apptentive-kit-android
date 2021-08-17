package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType

class EnjoymentDialogInteraction(
    id: String,
    val title: String,
    val yesText: String,
    val noText: String,
    val dismissText: String?
) : Interaction(id = id, type = InteractionType.EnjoymentDialog) {
    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id, title=\"$title\", yesText=\"$yesText\", noText=\"$noText\")"
    }

    companion object {
        const val TAG = "APPTENTIVE_LOVE_DIALOG"
    }
}
