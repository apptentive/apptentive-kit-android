package apptentive.com.android.feedback.message

import android.app.Activity
import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Observable
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.UnreadMessageCallback
import apptentive.com.android.feedback.backend.MessageCenterService
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.lifecycle.LifecycleListener
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID
import java.io.InputStream

/**
 * This class acts as a communicator between MessageCenterModule & Apptentive core components
 * It owns [PollingScheduler],schedules and updates the polling [Configuration] depending on whether
 * the App & MessageCenter is in the background vs foreground
 * Fetches,sorts & groups messages
 * Updates & maintain the cache using [MessageRepository]
 * Sends & download attachments
 * Supports hidden messages
 * Provides [UnreadMessageCallback] to notify new unread messages to the app
**/

@InternalUseOnly
class MessageManager(
    private val conversationId: String?,
    private val conversationToken: String?,
    private val messageCenterService: MessageCenterService,
    private val serialExecutor: Executor,
    private val messageRepository: MessageRepository
) : LifecycleListener, ConversationListener {

    private val messagesFromStorage: List<Message> = messageRepository.getAllMessages()
    private var hasSentMessage: Boolean = messagesFromStorage.isNotEmpty() // True after the first non hidden message is sent from mc client

    private var isMessageCenterInForeground = false
    private var lastDownloadedMessageID: String = messageRepository.getLastReceivedMessageIDFromEntries()
    var messageCustomData: Map<String, Any?>? = null

    @VisibleForTesting
    val pollingScheduler: PollingScheduler by lazy {
        MessagePollingScheduler(serialExecutor)
    }
    private val messagesSubject: BehaviorSubject<List<Message>> = BehaviorSubject(messagesFromStorage)
    val messages: Observable<List<Message>> get() = messagesSubject

    private var lastUnreadMessageCount = 0
    private var unreadMessageCountUpdate: (() -> Unit)? = null

    private val profileSubject: BehaviorSubject<Person?> = BehaviorSubject(null)
    val profile: Observable<Person?> get() = profileSubject

    private var configuration: Configuration = Configuration()
    private lateinit var senderProfile: Person

    private var fetchingInProgress = false

    override fun onAppBackground() {
        Log.d(MESSAGE_CENTER, "App is in the background, stop polling")
        stopPolling()
        messageRepository.saveMessages()
    }

    override fun onAppForeground() {
        if (hasSentMessage) {
            Log.d(MESSAGE_CENTER, "App is in the foreground & hasSentMessage is true, start polling")
            startPolling()
        }
    }

    override fun onConversationChanged(conversation: Conversation) {
        configuration = conversation.configuration
        senderProfile = conversation.person
        profileSubject.value = senderProfile
    }

    fun setCustomData(customData: Map<String, Any?>) {
        this.messageCustomData = customData
    }

    private fun clearCustomData() {
        this.messageCustomData = null
    }

    fun fetchMessages() {
        if (!fetchingInProgress && !conversationId.isNullOrEmpty() && !conversationToken.isNullOrEmpty()) {
            fetchingInProgress = true
            messageCenterService.getMessages(conversationToken, conversationId, lastDownloadedMessageID) {
                // Store the message list
                if (it is Result.Success) {
                    Log.d(MESSAGE_CENTER, "Fetch finished successfully")
                    // Merge the new messages with existing messages
                    val isMessageListUpdated =
                        mergeMessages(it.data.messages.orEmpty(), it.data.endsWith)

                    // Fetch until hasMore is true & not receiving empty list
                    fetchMoreIfNeeded(it.data.hasMore ?: false, isMessageListUpdated)
                } else {
                    Log.d(MESSAGE_CENTER, "Cannot fetch messages, conversationId/conversationToken is null or empty!")
                }
                fetchingInProgress = false
            }
        }
    }

    private fun mergeMessages(newMessages: List<Message>, endsWith: String?): Boolean {
        return if (newMessages.isNotEmpty()) {
            lastDownloadedMessageID = endsWith ?: messageRepository.getLastReceivedMessageIDFromEntries()
            // Update storage
            messageRepository.addOrUpdateMessages(
                newMessages.map { message ->
                    message.messageStatus = Message.Status.Saved
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
            Log.d(MESSAGE_CENTER, "All messages fetched")

            // Update unread messages callback
            val currentUnreadCount = getUnreadMessageCount()
            if (lastUnreadMessageCount != currentUnreadCount) {
                lastUnreadMessageCount = currentUnreadCount
                unreadMessageCountUpdate?.invoke()
            }
        }
    }

    fun sendMessage(messageText: String, attachments: List<Message.Attachment> = emptyList(), isHidden: Boolean? = null) {
        val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()
        val message = Message(
            type = if (attachments.isEmpty()) Message.MESSAGE_TYPE_TEXT else Message.MESSAGE_TYPE_COMPOUND,
            body = messageText,
            attachments = attachments,
            sender = Sender(senderProfile.id, senderProfile.name, null),
            hidden = isHidden,
            messageStatus = Message.Status.Sending,
            inbound = true,
            customData = messageCustomData
        )

        messageRepository.addOrUpdateMessages(listOf(message))
        messagesSubject.value = messageRepository.getAllMessages()

        context.sendPayload(message.toMessagePayload())
        clearCustomData()
        if (!hasSentMessage) {
            hasSentMessage = true
            startPolling()
        }
    }

    fun updateProfile(name: String?, email: String?) {
        name?.let { Apptentive.setPersonName(name) }
        email?.let { Apptentive.setPersonEmail(email) }
    }

    fun sendMessage(message: Message) {
        val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()
        messageRepository.addOrUpdateMessages(listOf(message))
        messagesSubject.value = messageRepository.getAllMessages()

        context.sendPayload(message.toMessagePayload())
        if (!hasSentMessage) {
            hasSentMessage = true
            startPolling()
        }
    }

    fun updateMessages(messages: List<Message>) {
        messageRepository.addOrUpdateMessages(messages)
    }

    fun sendAttachment(uri: String, isHidden: Boolean? = null) {
        try {
            val message = Message(
                type = Message.MESSAGE_TYPE_COMPOUND,
                body = null,
                sender = Sender(senderProfile.id, senderProfile.name, null),
                hidden = isHidden,
                messageStatus = Message.Status.Sending,
                inbound = true
            )

            /*
         * Make a local copy in the cache dir. By default the file name is "apptentive-api-file + nonce"
         * If original uri is known, the name will be taken from the original uri
         */
            val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext()
                .getAppActivity()
            FileUtil.createLocalStoredAttachment(activity, uri, message.nonce)?.let {
                it.id = message.nonce
                message.attachments = listOf(it)
                sendMessage(message)
            } ?: Log.e(
                MESSAGE_CENTER,
                "Issue with creating attachment file. Cannot send. Check logs."
            )
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Failed to send an attachment message", exception)
        }
    }

    fun sendHiddenAttachmentFromInputStream(inputStream: InputStream, mimeType: String) {
        try {
            val message = Message(
                type = Message.MESSAGE_TYPE_COMPOUND,
                body = null,
                sender = Sender(senderProfile.id, senderProfile.name, null),
                hidden = true,
                messageStatus = Message.Status.Sending,
                inbound = true
            )

            val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext()
                .getAppActivity()
            var localFilePath: String =
                FileUtil.generateCacheFilePathFromNonceOrPrefix(activity, message.nonce, null)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (!extension.isNullOrEmpty()) localFilePath += ".$extension"

            // When created from InputStream, there is no source file uri or path, so just use the cache file path
            FileUtil.createLocalStoredAttachmentFile(
                activity,
                inputStream,
                localFilePath,
                localFilePath,
                mimeType
            )?.let {
                it.id = message.nonce
                message.attachments = listOf(it)
                sendMessage(message)
            } ?: Log.e(
                MESSAGE_CENTER,
                "Issue with creating attachment file. Cannot send. Check logs."
            )
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Failed to send a hidden attachment")
        }
    }

    fun downloadAttachment(activity: Activity, message: Message, attachment: Message.Attachment) {
        val loadingAttachment = message.attachments?.onEach { if (it.id == attachment.id) it.isLoading = true }
        messageRepository.addOrUpdateMessages(listOf(message.copy(attachments = loadingAttachment)))
        messagesSubject.value = messageRepository.getAllMessages()

        messageCenterService.getAttachment(attachment.url.orEmpty()) { result ->
            val updatedMessage = if (result is Result.Success) {
                Log.d(MESSAGE_CENTER, "Image fetched successfully")

                val localFileLocation = FileUtil.generateCacheFilePathFromNonceOrPrefix(activity, attachment.id ?: generateUUID(), null)
                FileUtil.writeFileData(localFileLocation, result.data)
                message.copy(
                    attachments = message.attachments?.onEach {
                        if (it.id == attachment.id) {
                            it.localFilePath = localFileLocation
                            it.isLoading = false
                        }
                    }
                )
            } else {
                Log.e(MESSAGE_CENTER, "Error retrieving image", (result as Result.Error).error)
                message.copy(
                    attachments = message.attachments?.onEach {
                        if (it.id == attachment.id) it.isLoading = false
                    }
                )
            }

            messageRepository.addOrUpdateMessages(listOf(updatedMessage))
            messagesSubject.value = messageRepository.getAllMessages()
        }
    }

    // Listens to MessageCenterActivity's active status
    fun onMessageCenterLaunchStatusChanged(isActive: Boolean) {
        if (isActive) {
            messagesSubject.value = messageRepository.getAllMessages()
            // Fetch messages as soon as message center comes to foreground. Needed for migration
            fetchMessages()
        }
        isMessageCenterInForeground = isActive
        Log.d(MESSAGE_CENTER, "Message center foreground status $isActive")
        // Resets polling with the right polling interval
        if (hasSentMessage)
            startPolling(true)
    }

    // Fetches all the messages from message store
    fun getAllMessages(): List<Message> = messages.value

    fun getUnreadMessageCount(): Int {
        return getAllMessages().filter { it.read != true && !it.inbound }.size
    }

    fun addUnreadMessageListener(callback: (() -> Unit)) {
        unreadMessageCountUpdate = callback
    }

    @InternalUseOnly
    fun updateMessageStatus(isSuccess: Boolean, payloadData: PayloadData) {
        messageRepository.getAllMessages().find {
            it.nonce == payloadData.nonce
        }?.apply {
            messageStatus = if (isSuccess) Message.Status.Sent else Message.Status.Failed
        }?.also {
            messageRepository.addOrUpdateMessages(listOf(it))
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
