package apptentive.com.android.feedback.textmodal

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.util.getList
import apptentive.com.android.util.optString

internal class TextModalInteractionTypeConverter : InteractionTypeConverter<TextModalInteraction> {
    @Suppress("UNCHECKED_CAST")
    override fun convert(data: InteractionData) = TextModalInteraction(
        id = data.id,
        title = data.configuration.optString("title"),
        body = data.configuration.optString("body"),
        actions = data.configuration.getList("actions").map { action ->
            action as TextModalActionConfiguration
        }
    )
}
