package apptentive.com.android.feedback.textmodal

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.util.getList
import apptentive.com.android.util.optInt
import apptentive.com.android.util.optMap
import apptentive.com.android.util.optString

internal class TextModalInteractionTypeConverter : InteractionTypeConverter<TextModalInteraction> {
    @Suppress("UNCHECKED_CAST")
    override fun convert(data: InteractionData) = TextModalInteraction(
        id = data.id,
        title = data.configuration.optString("title"),
        body = data.configuration.optString("body"),
        maxHeight = data.configuration.optInt("max_height"),
        richContent = data.configuration.optMap("image")?.toRichContent(),
        actions = data.configuration.getList("actions").map { action ->
            action as TextModalActionConfiguration
        }
    )

    private fun Map<String, Any?>.toRichContent(): RichContent =
        RichContent(
            url = optString("url") ?: "",
            layout = optString("layout")?.toLayoutOptions() ?: LayoutOptions.FULL_WIDTH,
            alternateText = optString("alt_text"),
            scale = optInt("scale"),
        )

    private fun String.toLayoutOptions(): LayoutOptions =
        when (this) {
            "align_left" -> LayoutOptions.ALIGN_LEFT
            "align_right" -> LayoutOptions.ALIGN_RIGHT
            "full_width" -> LayoutOptions.FULL_WIDTH
            "center" -> LayoutOptions.CENTER
            else -> LayoutOptions.FULL_WIDTH
        }
}
