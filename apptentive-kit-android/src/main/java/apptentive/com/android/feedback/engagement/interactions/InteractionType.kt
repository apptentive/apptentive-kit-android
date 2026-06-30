package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class InteractionType(val name: String) {
    override fun toString(): String {
        return name
    }

    companion object {
        private const val ENJOYMENT_DIALOG = "EnjoymentDialog"
        private const val RATING_DIALOG = "RatingDialog"
        private const val MESSAGE_CENTER = "MessageCenter"
        private const val APP_STORE_RATING = "AppStoreRating"
        private const val GOOGLE_IN_APP_REVIEW = "InAppRatingDialog"
        private const val SURVEY = "Survey"
        private const val TEXT_MODAL = "TextModal"
        private const val NAVIGATE_TO_LINK = "NavigateToLink"
        private const val INITIATOR = "Initiator"

        val EnjoymentDialog = InteractionType(ENJOYMENT_DIALOG)
        val RatingDialog = InteractionType(RATING_DIALOG)
        val MessageCenter = InteractionType(MESSAGE_CENTER)
        val AppStoreRating = InteractionType(APP_STORE_RATING)
        val GoogleInAppReview = InteractionType(GOOGLE_IN_APP_REVIEW)
        val Survey = InteractionType(SURVEY)
        val TextModal = InteractionType(TEXT_MODAL)
        val NavigateToLink = InteractionType(NAVIGATE_TO_LINK)
        val Initiator = InteractionType(INITIATOR)

        internal fun names() = listOf(
            ENJOYMENT_DIALOG,
            RATING_DIALOG,
            MESSAGE_CENTER,
            APP_STORE_RATING,
            GOOGLE_IN_APP_REVIEW,
            SURVEY,
            TEXT_MODAL,
            NAVIGATE_TO_LINK,
            INITIATOR,
        )
    }
}
