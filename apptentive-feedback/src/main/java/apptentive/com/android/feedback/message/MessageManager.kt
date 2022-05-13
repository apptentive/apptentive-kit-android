package apptentive.com.android.feedback.message

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.backend.MessageFetchService
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.lifecycle.LifecycleListener
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.Result

@InternalUseOnly
class MessageManager(
    private val conversationId: String?,
    private val conversationToken: String?,
    private val messageFetchService: MessageFetchService
) : LifecycleListener {
    private val pollingInterval: Double = 30.00
    private var isMessageCenterUsed: Boolean = true

    override fun onBackground() {
        Log.d(MESSAGE_CENTER, "App is in the background, stop polling")
        stopPolling()
    }

    override fun onForeground() {
        Log.d(MESSAGE_CENTER, "App is in the foreground, start polling")
        if (isMessageCenterUsed)
            startPolling()
    }

    fun fetchMessages() {
        // Tie the logic with polling & lastDownloaded messageId
        if (!conversationId.isNullOrEmpty() && !conversationToken.isNullOrEmpty()) {
            messageFetchService.getMessages(conversationToken, conversationId) {
                // Store the message list
                if (it is Result.Success) {
                    Log.d(MESSAGE_CENTER, "${it.data}")
                }
            }
        }
    }

    // Test method to send message until UI is built
    fun sendMessage() {
        val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()
        val message = Message(
            type = "Text message",
            body = "Hello from new SDK 05/11 - part 2",
            sender = Sender("6274633684a1ff3c20c99ab3", null, null),
        )
        context.sendPayload(message.toMessagePayload())
    }

    private fun startPolling() {
        // fetchMessages()
        // sendMessage()
    }

    private fun stopPolling() {}
}
