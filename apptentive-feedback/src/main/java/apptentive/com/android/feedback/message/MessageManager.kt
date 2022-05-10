package apptentive.com.android.feedback.message

import apptentive.com.android.feedback.backend.MessageFetchService
import apptentive.com.android.feedback.lifecycle.LifecycleListener
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER

@InternalUseOnly
class MessageManager(
    private val conversationID: String?,
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
        if (!conversationID.isNullOrEmpty() && !conversationToken.isNullOrEmpty()) {
            messageFetchService.getMessages(conversationToken, conversationID) {
                // Store the message list
            }
        }
    }

    private fun startPolling() {}

    private fun stopPolling() {}
}
