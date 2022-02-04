package apptentive.com.android.feedback.textmodal

import apptentive.com.android.core.Provider


internal interface TextModalInteractionFactory {
    fun getTextModalInteraction(): TextModalInteraction
}

internal class TextModalInteractionProvider(val interaction: TextModalInteraction) : Provider<TextModalInteractionFactory> {
    override fun get(): TextModalInteractionFactory {
        return object : TextModalInteractionFactory {
            override fun getTextModalInteraction(): TextModalInteraction = interaction
        }
    }
}