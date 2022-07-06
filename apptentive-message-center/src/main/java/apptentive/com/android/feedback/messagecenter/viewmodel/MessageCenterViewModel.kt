package apptentive.com.android.feedback.messagecenter.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.feedback.dependencyprovider.MessageCenterModelFactory
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageCenterModel
import apptentive.com.android.feedback.utils.convertToGroupDate
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.isSame

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

class MessageCenterViewModel : ViewModel() {

    private val model = DependencyProvider.of<MessageCenterModelFactory>().messageCenterModel()
    private val executors = DependencyProvider.of<EngagementContextFactory>().engagementContext().executors
    private val context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext()
    private val messageManager: MessageManager = DependencyProvider.of<MessageManagerFactory>().messageManager()

    val title: String = model.title ?: ""
    val greeting: String = model.greeting?.title ?: ""
    val greetingBody: String = model.greeting?.body ?: ""
    val composerHint: String = model.composer?.hintText ?: ""
    var messages: List<Message> = groupMessages(getMessagesFromManager().filterHidden())
    val isFirstLaunch: Boolean = messages.isEmpty()

    private val newMessagesSubject = LiveEvent<List<Message>>()
    val newMessages: LiveData<List<Message>> get() = newMessagesSubject

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val clearMessage = LiveEvent<Boolean>()
    val clearMessageStream: LiveData<Boolean> = clearMessage

    private val messageObserver: (List<Message>) -> Unit = { newMessageList: List<Message> ->
        val newGroupedMessages = groupMessages(newMessageList.filterHidden())
        if (!messages.isSame(newGroupedMessages)) {
            executors.main.execute {
                val newMessage = newGroupedMessages.filterNot { messages.contains(it) }
                newMessagesSubject.value = newMessage
                messages = newGroupedMessages
            }
        }
    }

    init {
        messageManager.messages.observe(messageObserver)
    }

    override fun onCleared() {
        // Clears the observer
        messageManager.messages.removeObserver(messageObserver)
        super.onCleared()
    }

    @VisibleForTesting
    @InternalUseOnly
    fun groupMessages(messages: List<Message>): List<Message> {
        var lastGroupTimestamp: String? = null

        return messages.onEach { message ->
            val groupTimestamp = convertToGroupDate(message.createdAt)
            if (lastGroupTimestamp != groupTimestamp) {
                lastGroupTimestamp = groupTimestamp
                message.groupTimestamp = groupTimestamp
            }
        }
    }

    private fun getMessagesFromManager(): List<Message> = messageManager.getAllMessages()

    private fun List<Message>.filterHidden(): List<Message> = filterNot { it.hidden }

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

    fun onMessageViewStatusChanged(isActive: Boolean) {
        messageManager.onMessageCenterLaunchStatusChanged(isActive)
    }
}
