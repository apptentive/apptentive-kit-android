package apptentive.com.android.feedback.message

import android.app.Activity
import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Observable
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.backend.MessageCenterService
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.lifecycle.LifecycleListener
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
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
    @InternalUseOnly var messageCustomData: CustomData? = null

    @VisibleForTesting
    val pollingScheduler: PollingScheduler by lazy {
        MessagePollingScheduler(serialExecutor)
    }
    private val messagesSubject: BehaviorSubject<List<Message>> = BehaviorSubject(messagesFromStorage)
    val messages: Observable<List<Message>> get() = messagesSubject

    private val profileSubject: BehaviorSubject<Person?> = BehaviorSubject(null)
    val profile: Observable<Person?> get() = profileSubject

    private var configuration: Configuration = Configuration()
    private lateinit var senderProfile: Person

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

    @InternalUseOnly
    fun setCustomData(customData: CustomData) {
        this.messageCustomData = customData
    }

    private fun clearCustomData() {
        this.messageCustomData = null
    }

    @InternalUseOnly
    fun fetchMessages() {
        if (!conversationId.isNullOrEmpty() && !conversationToken.isNullOrEmpty()) {
            messageCenterService.getMessages(conversationToken, conversationId, lastDownloadedMessageID) {
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
        }
    }

    @InternalUseOnly
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
            customData = messageCustomData?.content
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

    @InternalUseOnly
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

    @InternalUseOnly
    fun sendAttachment(uri: String, isHidden: Boolean? = null) {
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
        val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext().getAppActivity()
        FileUtil.createLocalStoredAttachment(activity, uri, message.nonce)?.let {
            it.id = message.nonce
            message.attachments = listOf(it)
            sendMessage(message)
        } ?: Log.e(MESSAGE_CENTER, "Issue with creating attachment file. Cannot send.")
    }

    fun sendHiddenAttachmentFromInputStream(inputStream: InputStream, mimeType: String) {
        val message = Message(
            type = Message.MESSAGE_TYPE_COMPOUND,
            body = null,
            sender = Sender(senderProfile.id, senderProfile.name, null),
            hidden = true,
            messageStatus = Message.Status.Sending,
            inbound = true
        )

        val activity = DependencyProvider.of<EngagementContextFactory>().engagementContext().getAppActivity()
        var localFilePath: String = FileUtil.generateCacheFilePathFromNonceOrPrefix(activity, message.nonce, null)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        if (!extension.isNullOrEmpty()) localFilePath += ".$extension"

        // When created from InputStream, there is no source file uri or path, so just use the cache file path
        FileUtil.createLocalStoredAttachmentFile(activity, inputStream, localFilePath, localFilePath, mimeType)?.let {
            it.id = message.nonce
            message.attachments = listOf(it)
            sendMessage(message)
        } ?: Log.e(MESSAGE_CENTER, "Issue with creating attachment file. Cannot send.")
    }

    fun downloadAttachment(activity: Activity, message: Message, attachment: Message.Attachment, callback: () -> Unit) {
        messageCenterService.getAttachment(attachment.url.orEmpty()) { result ->
            if (result is Result.Success) {
                val localFileLocation = FileUtil.generateCacheFilePathFromNonceOrPrefix(activity, attachment.id ?: generateUUID(), null)
                FileUtil.writeFileData(localFileLocation, result.data)
                val updatedMessage = message.copy(
                    attachments = message.attachments?.also { attachments ->
                        attachments.find {
                            it.id == attachment.id
                        }?.localFilePath = localFileLocation
                    }
                )
                messageRepository.addOrUpdateMessages(listOf(updatedMessage))
                Log.d(MESSAGE_CENTER, "Image fetched successfully")
                callback()
            } else {
                Log.e(MESSAGE_CENTER, "Error retrieving image", (result as Result.Error).error)
                callback()
            }
        }
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
