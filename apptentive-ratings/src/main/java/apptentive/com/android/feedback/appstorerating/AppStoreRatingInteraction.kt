package apptentive.com.android.feedback.appstorerating

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType

internal class AppStoreRatingInteraction(
    id: InteractionId,
    val storeID: String?,
    val method: String?,
    val url: String?,
) : Interaction(id, InteractionType.AppStoreRating) {
    override fun toString(): String {
        return "${javaClass.simpleName} (id=$id, store_id=\"$storeID\", method=\"$method\", url=\"$url\")"
    }

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppStoreRatingInteraction) return false

        if (storeID != other.storeID) return false
        if (method != other.method) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = storeID?.hashCode() ?: 0
        result = 31 * result + (method?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

    //endregion
}