package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionData

import apptentive.com.android.util.getString
import apptentive.com.android.util.optString

internal class EnjoymentDialogInteractionTypeConverter : InteractionTypeConverter<EnjoymentDialogInteraction> {
    override fun convert(data: InteractionData) = EnjoymentDialogInteraction(
        id = data.id,
        title = data.configuration.getString("title"),
        yesText = data.configuration.getString("yes_text"),
        noText = data.configuration.getString("no_text"),
        dismissText = data.configuration.optString("dismiss_text")
    )
}