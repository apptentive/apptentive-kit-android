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
        richContent = data.configuration.optMap("image")?.toRichContent(data.configuration),
        actions = data.configuration.getList("actions").map { action ->
            action as TextModalActionConfiguration
        }
    )

    private fun Map<String, Any?>.toRichContent(configuration: Map<String, *>): RichContent =
        RichContent(
            url = optString("url") ?: "",
            layout = optString("layout")?.toLayoutOptions() ?: LayoutOptions.FILL,
            alternateText = optString("alt_text"),
            maxHeight = configuration.optInt("max_height")
        )

    private fun String.toLayoutOptions(): LayoutOptions =
        when (this) {
            "leading" -> LayoutOptions.ALIGN_LEFT
            "trailing" -> LayoutOptions.ALIGN_RIGHT
            "fill" -> LayoutOptions.FILL
            "fit" -> LayoutOptions.FIT
            "center" -> LayoutOptions.CENTER
            else -> LayoutOptions.FILL
        }
}
