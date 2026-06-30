package apptentive.com.android.feedback.dependencyprovider

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.messagecenter.utils.convertToMessageCenterModel
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getEngagementContext
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getMessageManager
import apptentive.com.android.feedback.utils.getInteractionBackup

internal fun createMessageCenterViewModel(
    context: EngagementContext = getEngagementContext(),
    messageManager: MessageManager = getMessageManager()
): MessageCenterViewModel {
    return try {
        MessageCenterViewModel(
            messageCenterModel = DependencyProvider.of<MessageCenterModelFactory>().messageCenterModel(),
            executors = context.executors,
            context = context,
            messageManager = messageManager
        )
    } catch (exception: Exception) {
        MessageCenterViewModel(
            messageCenterModel = (getInteractionBackup() as MessageCenterInteraction).convertToMessageCenterModel(),
            executors = context.executors,
            context = context,
            messageManager = messageManager
        )
    }
}
