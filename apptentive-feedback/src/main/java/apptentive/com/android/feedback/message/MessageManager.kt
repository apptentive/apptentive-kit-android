package apptentive.com.android.feedback.message

import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
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
) : LifecycleListener, ConversationListener {
    private var isMessageCenterUsed: Boolean = true
    private var isMessageCenterInForeground = true
    private val pollingScheduler: PollingScheduler by lazy {
        MessagePollingScheduler(serialExecutor)
    }

    private lateinit var configuration: Configuration
    private lateinit var senderProfile: Person

    override fun onAppBackground() {
        Log.d(MESSAGE_CENTER, "App is in the background, stop polling")
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

    private fun fetchMessages() {
        // Tie the logic with polling & lastDownloaded messageId
        if (!conversationId.isNullOrEmpty() && !conversationToken.isNullOrEmpty()) {
            messageFetchService.getMessages(conversationToken, conversationId) {
                // Store the message list
                if (it is Result.Success) {
                    Log.d(MESSAGE_CENTER, "Fetch finished successfully ${it.data}")
                    pollingScheduler.onFetchFinish()
                } else {
                    Log.d(MESSAGE_CENTER, "There is an issue in the message fetch")
                }
            }
        }
    }

    // Test method to send message until UI is built
    fun sendMessage(message: String) {
        val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()
        val message = Message(
            type = "Text message",
            body = message,
            sender = Sender(senderProfile.id, senderProfile.name, null),
        )
        context.sendPayload(message.toMessagePayload())
    }

    // Listens to MessageCenterActivity's active status
    fun onMessageCenterLaunchStatusChanged(isActive: Boolean) {
        isMessageCenterInForeground = isActive
        // Resets polling with the right polling interval
        startPolling(true)
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
