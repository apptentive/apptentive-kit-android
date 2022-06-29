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
    private var isMessageCenterUsed: Boolean = true
    private var isMessageCenterInForeground = false
    private var lastDownloadedMessageID: String = messageRepository.getLastReceivedMessageIDFromEntries()
    @VisibleForTesting
    val pollingScheduler: PollingScheduler by lazy {
        MessagePollingScheduler(serialExecutor)
    }
    private val messagesSubject: BehaviorSubject<List<Message>> = BehaviorSubject(messageRepository.getAllMessages())
    val messages: Observable<List<Message>> get() = messagesSubject

    private var configuration: Configuration = Configuration()
    private lateinit var senderProfile: Person

    override fun onAppBackground() {
        Log.d(MESSAGE_CENTER, "App is in the background, stop polling")
        messageRepository.saveMessages()
        stopPolling()
    }

    override fun onAppForeground() {
        Log.d(MESSAGE_CENTER, "App is in the foreground, start polling")
        if (isMessageCenterUsed)
            startPolling()
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
                    Log.d(MESSAGE_CENTER, "Fetch finished successfully ${it.data}")
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

    // Test method to send message until UI is built
    fun sendMessage(message: String) {
        val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()
        val message = Message(
            // TODO revisit type field
            type = "Text",
            body = message,
            sender = Sender(senderProfile.id, senderProfile.name, null),
        )
        context.sendPayload(message.toMessagePayload())
    }

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
        startPolling(true)
    }

    // Fetches all the messages from message store
    fun getAllMessages(): List<Message> = messages.value

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
