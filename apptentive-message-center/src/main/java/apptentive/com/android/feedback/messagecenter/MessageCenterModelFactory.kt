package apptentive.com.android.feedback.messagecenter

import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.messagecenter.interaction.MessageCenterInteraction
import apptentive.com.android.feedback.messagecenter.model.MessageCenterModel
import apptentive.com.android.feedback.messagecenter.utils.convertToMessageCenterModel

internal interface MessageCenterModelFactory {
    fun messageCenterModel(): MessageCenterModel
}

internal class MessageCenterModelProvider(
    val interaction: MessageCenterInteraction
) : Provider<MessageCenterModelFactory> {
    override fun get(): MessageCenterModelFactory {
        return object : MessageCenterModelFactory {
            override fun messageCenterModel(): MessageCenterModel {
                return interaction.convertToMessageCenterModel()
            }
        }
    }
}
