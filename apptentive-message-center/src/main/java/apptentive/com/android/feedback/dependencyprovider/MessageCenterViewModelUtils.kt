package apptentive.com.android.feedback.dependencyprovider

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.feedback.model.MessageCenterModel

internal fun createMessageCenterViewModel(
    messageCenterModel: MessageCenterModel = DependencyProvider.of<MessageCenterModelFactory>().messageCenterModel(),
    context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext(),
    messageManager: MessageManager = DependencyProvider.of<MessageManagerFactory>().messageManager()
) = MessageCenterViewModel(
    messageCenterModel = messageCenterModel,
    executors = context.executors,
    context = context,
    messageManager = messageManager
)
