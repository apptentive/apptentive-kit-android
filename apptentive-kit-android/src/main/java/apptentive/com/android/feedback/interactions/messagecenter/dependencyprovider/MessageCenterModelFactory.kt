package apptentive.com.android.feedback.interactions.messagecenter.dependencyprovider

import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.interactions.messagecenter.utils.convertToMessageCenterModel
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.model.MessageCenterModel

internal interface MessageCenterModelFactory {
    fun messageCenterModel(): MessageCenterModel
}

internal class MessageCenterModelProvider(
    val interaction: MessageCenterInteraction
) : Provider<MessageCenterModelFactory> {
    override fun get(): apptentive.com.android.feedback.interactions.messagecenter.dependencyprovider.MessageCenterModelFactory {
        return object :
            apptentive.com.android.feedback.interactions.messagecenter.dependencyprovider.MessageCenterModelFactory {
            override fun messageCenterModel(): MessageCenterModel {
                return interaction.convertToMessageCenterModel()
            }
        }
    }
}
