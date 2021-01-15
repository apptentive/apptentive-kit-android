package apptentive.com.android.feedback.engagement.interactions

data class InteractionType(private val name: String) {
    override fun toString(): String {
        return name
    }

    companion object {
        private const val UPGRADE_MESSAGE = "UpgradeMessage"
        private const val ENJOYMENT_DIALOG = "EnjoymentDialog"
        private const val RATING_DIALOG = "RatingDialog"
        private const val MESSAGE_CENTER = "MessageCenter"
        private const val APP_STORE_RATING = "AppStoreRating"
        private const val SURVEY = "Survey"
        private const val TEXT_MODAL = "TextModal"
        private const val NAVIGATE_TO_LINK = "NavigateToLink"

        val UpgradeMessage = InteractionType(UPGRADE_MESSAGE)
        val EnjoymentDialog = InteractionType(ENJOYMENT_DIALOG)
        val RatingDialog = InteractionType(RATING_DIALOG)
        val MessageCenter = InteractionType(MESSAGE_CENTER)
        val AppStoreRating = InteractionType(APP_STORE_RATING)
        val Survey = InteractionType(SURVEY)
        val TextModal = InteractionType(TEXT_MODAL)
        val NavigateToLink = InteractionType(NAVIGATE_TO_LINK)

        fun names() = listOf(
            UPGRADE_MESSAGE,
            ENJOYMENT_DIALOG,
            RATING_DIALOG,
            MESSAGE_CENTER,
            APP_STORE_RATING,
            SURVEY,
            TEXT_MODAL,
            NAVIGATE_TO_LINK
        )
    }
}