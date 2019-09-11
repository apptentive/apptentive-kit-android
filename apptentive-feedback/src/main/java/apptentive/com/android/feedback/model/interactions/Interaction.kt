package apptentive.com.android.feedback.model.interactions

class Interaction(
    val id: String,
    val type: Type,
    val displayType: DisplayType,
    val version: Int

) {
    enum class Type {
        UpgradeMessage,
        EnjoymentDialog,
        RatingDialog,
        MessageCenter,
        AppStoreRating,
        Survey,
        TextModal,
        NavigateToLink,
        unknown;

        companion object {
            fun parse(type: String): Type {
                return try {
                    valueOf(type)
                } catch (e: IllegalArgumentException) {
                    unknown
                }
            }
        }
    }

    enum class DisplayType {
        notification,
        unknown;

        companion object {
            fun parse(type: String): DisplayType {
                return try {
                    valueOf(type)
                } catch (e: Exception) {
                    unknown
                }
            }
        }
    }
}