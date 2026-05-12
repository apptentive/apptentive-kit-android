package apptentive.com.android.feedback.textmodal

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.ui.DialogPosition

internal typealias TextModalActionConfiguration = Map<String, Any?>

internal class TextModalInteraction(
    id: InteractionId,
    val title: String?,
    val body: String?,
    val maxHeight: Int = 100,
    val richContent: RichContent? = null,
    val actions: List<TextModalActionConfiguration>,
    val position: DialogPosition = DialogPosition.CENTER,
    val verticalMargins: Int? = null,
) : Interaction(id, InteractionType.TextModal) {

    override fun toString(): String {
        return "${javaClass.simpleName} (id=$id, title=\"$title\", body=\"$body\", maxHeight=$maxHeight, richContent=\"$richContent\", position=$position, verticalMargins=$verticalMargins,  actions=$actions)"
    }

    companion object {
        const val TAG = "APPTENTIVE_NOTE_DIALOG"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextModalInteraction) return false

        if (title != other.title) return false
        if (body != other.body) return false
        if (maxHeight != other.maxHeight) return false
        if (richContent != other.richContent) return false
        if (actions != other.actions) return false
        if (position != other.position) return false
        if (verticalMargins != other.verticalMargins) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + maxHeight
        result = 31 * result + (richContent?.hashCode() ?: 0)
        result = 31 * result + actions.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (verticalMargins ?: 0)
        return result
    }
}
