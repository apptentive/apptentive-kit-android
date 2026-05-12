package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.ui.DialogPosition

internal class EnjoymentDialogInteraction(
    id: String,
    val title: String,
    val yesText: String,
    val noText: String,
    val dismissText: String?,
    val position: DialogPosition = DialogPosition.CENTER,
    val verticalMargins: Int? = null,
) : Interaction(id = id, type = InteractionType.EnjoymentDialog) {
    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id, title=\"$title\", yesText=\"$yesText\", noText=\"$noText\", dismissText=\"$dismissText\", position=$position, verticalMargins=$verticalMargins)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EnjoymentDialogInteraction

        if (id != other.id) return false
        if (title != other.title) return false
        if (yesText != other.yesText) return false
        if (noText != other.noText) return false
        if (dismissText != other.dismissText) return false
        if (position != other.position) return false
        if (verticalMargins != other.verticalMargins) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + yesText.hashCode()
        result = 31 * result + noText.hashCode()
        result = 31 * result + (dismissText?.hashCode() ?: 0)
        result = 31 * result + position.hashCode()
        result = 31 * result + (verticalMargins ?: 0)
        return result
    }

    companion object {
        const val TAG = "APPTENTIVE_LOVE_DIALOG"
    }
}
