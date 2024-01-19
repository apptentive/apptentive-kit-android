package apptentive.com.android.feedback.textmodal

import apptentive.com.android.core.Provider

internal interface TextModalModelFactory {
    fun getTextModalModel(): TextModalModel
}

internal class TextModalInteractionProvider(val interaction: TextModalInteraction) : Provider<TextModalModelFactory> {
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
                    }
                )
            }
        }
    }
}
