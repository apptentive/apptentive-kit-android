package apptentive.com.android.feedback.messagecenter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents
import apptentive.com.android.feedback.model.MessageCenterModel

/**
 * ViewModel for MessageCenter
 *
 * MessageCenterViewModel class is responsible for preparing and managing MessageCenter data
 * for MessageCenter views
 *
 * @property model [MessageCenterModel] data model that represents the MessageCenter
 * @property executors [Executors] executes submitted runnable tasks in main/background threads.
 *
 *  Apptentive uses two executors
 *
 *    * state - For long running/ Async operations
 *    * main  - UI related tasks
 *
 */

class MessageCenterViewModel(
    private val model: MessageCenterModel,
    private val executors: Executors,
) : ViewModel() {
    private val context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext()
    private val messageManager: MessageManager = DependencyProvider.of<MessageManagerFactory>().messageManager()
    val title: String = model.title ?: ""

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val clearMessage = LiveEvent<Boolean>()
    val clearMessageStream: LiveData<Boolean> = clearMessage

    fun exitMessageCenter() {
        onMessageCenterEvent(MessageCenterEvents.EVENT_NAME_CLOSE)
        exitEvent.postValue(true)
    }

    fun sendMessage(message: String) {
        clearMessage.postValue(true)
        messageManager.sendMessage(message)
    }

    fun onMessageCenterEvent(event: String) {
        when (event) {
            MessageCenterEvents.EVENT_NAME_CLOSE -> {
                context.engage(
                    event = Event.internal(event, interaction = InteractionType.MessageCenter),
                    interactionId = model.interactionId
                )
            }
        }
    }
}
