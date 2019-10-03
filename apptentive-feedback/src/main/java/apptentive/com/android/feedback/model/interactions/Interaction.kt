package apptentive.com.android.feedback.model.interactions

import android.net.Uri
import apptentive.com.android.feedback.model.interactions.NavigateToLinkInteraction.Target
import apptentive.com.android.feedback.utils.*

// TODO: turn it into a configurable factory so other interaction types could be registered
open class Interaction(val id: String) {
    companion object {
        fun fromJson(json: Map<String, *>): Interaction {
            val id = json.getString("id")
            val type = json.getString("type")
            val displayType = json.optString("display_type")
            val configuration = json.getMap("configuration")

            @Suppress("UNCHECKED_CAST")
            return when (type) {
                "UpgradeMessage" -> UpgradeMessageInteraction(
                    id = id,
                    active = configuration.optBoolean("active") ?: false,
                    appVersion = configuration.optString("app_version"),
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
                "RatingDialog" -> RatingDialogInteraction(
                    id = id,
                    title = configuration.getString("title"),
                    body = configuration.getString("body"),
                    rateText = configuration.getString("rate_text"),
                    remindText = configuration.getString("remind_text"),
                    declineText = configuration.getString("decline_text")
                )
                "MessageCenter" -> MessageCenterInteraction(id = id) // FIXME: parse message center interaction
                "AppStoreRating" -> AppStoreRatingInteraction(id = id)
                "Survey" -> SurveyInteraction(
                    id = id,
                    name = configuration.getString("name"),
                    description = configuration.optString("description"),
                    successMessage = configuration.optString("success_message"),
                    requiredText = configuration.optString("required_text"),
                    submitText = configuration.getString("submit_text"),
                    validationError = configuration.getString("validation_error"),
                    questions = (configuration.getList("questions") as List<Map<String, *>>).map {
                        SurveyQuestion.fromJson(it)
                    }
                )
                "TextModal" -> TextModalInteraction(
                    id = id,
                    title = configuration.getString("title"),
                    body = configuration.getString("body"),
                    actions = (configuration.getList("actions") as List<Map<String, *>>).map {
                        TextModalAction.fromJson(it)
                    }
                )
                "NavigateToLink" -> NavigateToLinkInteraction(
                    id = id,
                    url = Uri.parse(configuration.getString("url")),
                    target = Target.tryParse(configuration.optString("target")) ?: Target.New
                )
                else -> UnknownInteraction
            }
        }
    }

}