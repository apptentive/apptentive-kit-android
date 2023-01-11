package apptentive.com.android.feedback.dependencyprovider

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.messagecenter.utils.convertToMessageCenterModel
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.feedback.utils.getInteractionBackup

internal fun createMessageCenterViewModel(
    context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext(),
    messageManager: MessageManager = DependencyProvider.of<MessageManagerFactory>().messageManager()
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
            messageCenterModel = (getInteractionBackup(context.getAppActivity()) as MessageCenterInteraction).convertToMessageCenterModel(),
            executors = context.executors,
            context = context,
            messageManager = messageManager
        )
    }
}
