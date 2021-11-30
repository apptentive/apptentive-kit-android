package apptentive.com.android.feedback.ratingdialog

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType

internal class RatingDialogInteraction(
    id: InteractionId,
    val title: String?,
    val body: String?,
    val rateText: String?,
    val remindText: String?,
    val declineText: String?
) : Interaction(id, InteractionType.RatingDialog) {
    override fun toString(): String {
        return "${javaClass.simpleName} (id=$id, title=\"$title\", body=\"$body\", " +
                "rate_text=\"$rateText\", remind_text=\"$remindText\", decline_text=\"$declineText\")"
    }

    companion object {
        const val TAG = "APPTENTIVE_REMIND_DIALOG"
    }

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RatingDialogInteraction) return false

        if (title != other.title) return false
        if (body != other.body) return false
        if (rateText != other.rateText) return false
        if (remindText != other.remindText) return false
        if (declineText != other.declineText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + (rateText?.hashCode() ?: 0)
        result = 31 * result + (remindText?.hashCode() ?: 0)
        result = 31 * result + (declineText?.hashCode() ?: 0)
        return result
    }

    //endregion
}