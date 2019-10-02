package apptentive.com.android.feedback.model.interactions

import apptentive.com.android.feedback.utils.*

open class Interaction(val id: String) {
    companion object {
        fun fromJson(json: Map<String, *>): Interaction {
            val id = json.getString("id")
            val type = json.getString("type")
            val priority = json.optInt("priority")
            val version = json.optString("version")
            val displayType = json.optString("display_type")
            val configuration = json.getMap("configuration")

            return when (type) {
                "UpgradeMessage" -> UpgradeMessageInteraction(
                    id = id,
                    active = configuration.getBoolean("active"),
                    appVersion = configuration.getString("app_version"),
                    showAppIcon = configuration.getBoolean("show_app_icon"),
                    showPoweredBy = configuration.getBoolean("show_powered_by"),
                    body = configuration.getString("body")
                )
                "EnjoymentDialog" -> EnjoymentDialogInteraction(
                    id = id,
                    title = configuration.getString("title"),
                    yesText = configuration.getString("yes_text"),
                    noText = configuration.getString("no_text"),
                    dismissText = configuration.optString("dismiss_text")
                )
                "RatingDialog" -> TODO()
                "MessageCenter" -> TODO()
                "AppStoreRating" -> TODO()
                "Survey" -> TODO()
                "TextModal" -> TODO()
                "NavigateToLink" -> TODO()
                else -> UnknownInteraction
            }
        }
    }

}