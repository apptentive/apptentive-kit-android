package apptentive.com.android.feedback.interactions.textmodal

import apptentive.com.android.core.Provider

internal interface TextModalModelFactory {
    fun getTextModalModel(): TextModalModel
}

internal class TextModalInteractionProvider(val interaction: TextModalInteraction, val whereEvent: String?) : Provider<TextModalModelFactory> {
    override fun get(): TextModalModelFactory {
        return object : TextModalModelFactory {
            override fun getTextModalModel(): TextModalModel {
                return TextModalModel(
                    id = interaction.id,
                    title = interaction.title,
                    body = interaction.body,
                    maxHeight = interaction.maxHeight,
                    richContent = interaction.richContent,
                    actions = interaction.actions.map { action ->
                        DefaultTextModalActionConverter().convert(action)
                    },
                    position = interaction.position,
                    verticalMargins = interaction.verticalMargins,
                    whereEvent = whereEvent,
                )
            }
        }
    }
}
