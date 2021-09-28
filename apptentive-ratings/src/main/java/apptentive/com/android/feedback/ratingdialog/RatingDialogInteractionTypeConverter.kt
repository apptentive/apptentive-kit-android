package apptentive.com.android.feedback.ratingdialog

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.util.optString

internal class RatingDialogInteractionTypeConverter : InteractionTypeConverter<RatingDialogInteraction> {
    @Suppress("UNCHECKED_CAST")
    override fun convert(data: InteractionData) = RatingDialogInteraction(
        id = data.id,
        title = data.configuration.optString("title"),
        body = data.configuration.optString("body"),
        rateText = data.configuration.optString("rate_text"),
        remindText = data.configuration.optString("remind_text"),
        declineText = data.configuration.optString("decline_text")
    )
}
