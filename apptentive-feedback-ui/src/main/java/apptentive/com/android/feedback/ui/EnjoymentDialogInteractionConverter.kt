package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.InteractionConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionData

import apptentive.com.android.util.getString
import apptentive.com.android.util.optString

internal class EnjoymentDialogInteractionConverter : InteractionConverter<EnjoymentDialogInteraction> {
    override fun convert(data: InteractionData) = EnjoymentDialogInteraction(
        id = data.id,
        title = data.configuration.getString("title"),
        yesText = data.configuration.getString("yes_text"),
        noText = data.configuration.getString("no_text"),
        dismissText = data.configuration.optString("dismiss_text")
    )
}