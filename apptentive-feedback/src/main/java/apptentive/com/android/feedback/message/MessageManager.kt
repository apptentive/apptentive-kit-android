package apptentive.com.android.feedback.message

import androidx.annotation.VisibleForTesting
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Observable
import apptentive.com.android.feedback.backend.MessageFetchService
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.lifecycle.LifecycleListener
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.Result

@InternalUseOnly
class MessageManager(
    private val conversationId: String?,
    private val conversationToken: String?,
    private val messageFetchService: MessageFetchService,
    private val serialExecutor: Executor,
    private val messageRepository: MessageRepository,
) : LifecycleListener, ConversationListener {

    private val messagesFromStorage: List<Message> = messageRepository.getAllMessages()
    private var hasSentMessage: Boolean = messagesFromStorage.isNotEmpty() // True after the first non hidden message is sent from mc client

    private var isMessageCenterInForeground = false
    private var lastDownloadedMessageID: String = messageRepository.getLastReceivedMessageIDFromEntries()
    @VisibleForTesting
    val pollingScheduler: PollingScheduler by lazy {
        MessagePollingScheduler(serialExecutor)
    }
    private val messagesSubject: BehaviorSubject<List<Message>> = BehaviorSubject(messagesFromStorage)
    val messages: Observable<List<Message>> get() = messagesSubject

    private var configuration: Configuration = Configuration()
    private lateinit var senderProfile: Person

    override fun onAppBackground() {
        Log.d(MESSAGE_CENTER, "App is in the background, stop polling")
        stopPolling()
        messageRepository.saveMessages()
    }

    override fun onAppForeground() {
        if (hasSentMessage) {
            Log.d(MESSAGE_CENTER, "App is in the foreground & canTriggerBgPoll is true, start polling")
            startPolling()
        }
    }

    override fun onConversationChanged(conversation: Conversation) {
        configuration = conversation.configuration
        senderProfile = conversation.person
    }

    @InternalUseOnly
    fun fetchMessages() {
        if (!conversationId.isNullOrEmpty() && !conversationToken.isNullOrEmpty()) {
            messageFetchService.getMessages(conversationToken, conversationId, lastDownloadedMessageID) {
                // Store the message list
                if (it is Result.Success) {
                    Log.d(MESSAGE_CENTER, "Fetch finished successfully")
                    // Merge the new messages with existing messages
                    val isMessageListUpdated =
                        mergeMessages(it.data.messages ?: listOf(), it.data.endsWith)
                    // Fetch until hasMore is true & not receiving empty list
                    fetchMoreIfNeeded(it.data.hasMore ?: false, isMessageListUpdated)
                } else {
                    Log.d(MESSAGE_CENTER, "Cannot fetch messages, conversationId/conversationToken is null or empty!")
                }
            }
        }
    }

    private fun mergeMessages(newMessages: List<Message>, endsWith: String?): Boolean {
        return if (newMessages.isNotEmpty()) {
            lastDownloadedMessageID = endsWith ?: messageRepository.getLastReceivedMessageIDFromEntries()
            // Update storage
            messageRepository.addOrUpdateMessage(
                newMessages.map { message ->
                    message.messageStatus = Message.Status.Saved
                    // TODO revisit type field
                    message.type = "Text"
                    message
                }
            )
            messagesSubject.value = messageRepository.getAllMessages()
            true
        } else false
    }

    private fun fetchMoreIfNeeded(hasMore: Boolean, receivedNonEmptyMessage: Boolean) {
        if (hasMore && receivedNonEmptyMessage) {
            Log.d(MESSAGE_CENTER, "Fetch messages after lastDownloadedMessageID $lastDownloadedMessageID")
            fetchMessages()
        } else {
            pollingScheduler.onFetchFinish()
        }
    }

    @InternalUseOnly
    fun sendMessage(messageText: String, isHidden: Boolean = false) {
        val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()
        val message = Message(
            // TODO revisit type field
            type = "Text",
            body = messageText,
            sender = Sender(senderProfile.id, senderProfile.name, null),
            hidden = isHidden,
            messageStatus = Message.Status.Sending,
            inbound = true
        )

        messageRepository.addOrUpdateMessage(listOf(message))
        messagesSubject.value = messageRepository.getAllMessages()

        context.sendPayload(message.toMessagePayload())

        if (!hasSentMessage) hasSentMessage = true
    }

    @InternalUseOnly
    // Listens to MessageCenterActivity's active status
    fun onMessageCenterLaunchStatusChanged(isActive: Boolean) {
        if (isActive) {
            messagesSubject.value = messageRepository.getAllMessages()
            // Fetch messages as soon as message center comes to foreground
            fetchMessages()
        }
        isMessageCenterInForeground = isActive
        Log.d(MESSAGE_CENTER, "Message center foreground status $isActive")
        // Resets polling with the right polling interval
        if (hasSentMessage)
            startPolling(true)
    }

    @InternalUseOnly
    // Fetches all the messages from message store
    fun getAllMessages(): List<Message> = messages.value

    @InternalUseOnly
    fun updateMessageStatus(isSuccess: Boolean, payloadData: PayloadData) {
        messageRepository.getAllMessages().find {
            it.nonce == payloadData.nonce
        }?.apply {
            messageStatus = if (isSuccess) Message.Status.Sent else Message.Status.Failed
        }?.also {
            messageRepository.addOrUpdateMessage(listOf(it))
            messagesSubject.value = messageRepository.getAllMessages()
        }
    }

    private fun startPolling(resetPolling: Boolean = false) {
        val delay =
            if (isMessageCenterInForeground) configuration.messageCenter.fgPoll
            else configuration.messageCenter.bgPoll
        Log.d(MESSAGE_CENTER, "Polling interval is set to $delay")
        pollingScheduler.startPolling(delay, resetPolling) {
            fetchMessages()
        }
    }

    private fun stopPolling() {
        pollingScheduler.stopPolling()
    }
}
