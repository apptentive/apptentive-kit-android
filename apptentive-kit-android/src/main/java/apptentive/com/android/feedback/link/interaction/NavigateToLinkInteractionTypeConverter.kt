package apptentive.com.android.feedback.link.interaction

import apptentive.com.android.core.util.getString
import apptentive.com.android.core.util.optString
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter

internal class NavigateToLinkInteractionTypeConverter : InteractionTypeConverter<NavigateToLinkInteraction> {
    override fun convert(data: InteractionData) = NavigateToLinkInteraction(
        id = data.id,
        url = data.configuration.getString("url"),
        target = NavigateToLinkInteraction.Target.parse(data.configuration.optString("target"))
    )
}
