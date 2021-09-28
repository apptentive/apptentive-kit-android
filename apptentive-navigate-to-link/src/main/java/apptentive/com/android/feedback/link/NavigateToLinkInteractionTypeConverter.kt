package apptentive.com.android.feedback.link

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.util.getString
import apptentive.com.android.util.optString

internal class NavigateToLinkInteractionTypeConverter : InteractionTypeConverter<NavigateToLinkInteraction> {
    override fun convert(data: InteractionData) = NavigateToLinkInteraction(
        id = data.id,
        url = data.configuration.getString("url"),
        target = NavigateToLinkInteraction.Target.parse(data.configuration.optString("target"))
    )
}
