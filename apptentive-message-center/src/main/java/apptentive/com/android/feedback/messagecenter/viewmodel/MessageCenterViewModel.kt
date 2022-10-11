package apptentive.com.android.feedback.messagecenter.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.BehaviorSubject
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
import apptentive.com.android.feedback.messagecenter.view.GreetingData
import apptentive.com.android.feedback.messagecenter.view.ListItemType
import apptentive.com.android.feedback.messagecenter.view.MessageViewData
import apptentive.com.android.feedback.messagecenter.view.ProfileViewData
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageCenterModel
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.ImageUtil
import apptentive.com.android.feedback.utils.convertToGroupDate
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.generateUUID

/**
 * ViewModel for MessageCenter
 *
 * MessageCenterViewModel class is responsible for preparing and managing MessageCenter data
 * for MessageCenter views
 *
 * @property messageCenterModel [MessageCenterModel] data model that represents the MessageCenter
 * @property executors [Executors] executes submitted runnable tasks in main/background threads.
 *
 *  Apptentive uses two executors
 *
 *    * state - For long running/ Async operations
 *    * main  - UI related tasks
 *
 */

@InternalUseOnly
class MessageCenterViewModel : ViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val messageCenterModel = DependencyProvider.of<MessageCenterModelFactory>().messageCenterModel()
    private val executors = DependencyProvider.of<EngagementContextFactory>().engagementContext().executors
    private val context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext()
    private val messageManager: MessageManager = DependencyProvider.of<MessageManagerFactory>().messageManager()

    val title: String = messageCenterModel.title.orEmpty()
    private val greeting: String = messageCenterModel.greeting?.title.orEmpty()
    private val greetingBody: String = messageCenterModel.greeting?.body.orEmpty()
    private val avatarUrl: String? = messageCenterModel.greeting?.image
    val composerHint: String = messageCenterModel.composer?.hintText.orEmpty()
    val messageSLA: String = messageCenterModel.status?.body.orEmpty()
    var messages: List<Message> = messageManager.getAllMessages().filterAndGroupMessages()
    var hasAutomatedMessage: Boolean = !messageCenterModel.automatedMessage?.body.isNullOrEmpty()
    var shouldCollectProfileData: Boolean = isProfileViewVisible()
    private var isAvatarLoading: Boolean = false
    private var isSendingMessage: Boolean = false

    private val newMessagesEvent = LiveEvent<List<MessageViewData>>()
    val newMessages: LiveData<List<MessageViewData>> get() = newMessagesEvent

    private val draftAttachmentsSubject = MutableLiveData<List<Message.Attachment>>()
    val draftAttachmentsStream: LiveData<List<Message.Attachment>> = draftAttachmentsSubject

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val clearMessageEvent = LiveEvent<Boolean>()
    val clearMessageStream: LiveData<Boolean> = clearMessageEvent

    private val errorMessagesEvent = LiveEvent<ValidationDataModel>()
    val errorMessagesStream: LiveData<ValidationDataModel> = errorMessagesEvent

    private val avatarBitmapEvent = LiveEvent<Bitmap>()
    val avatarBitmapStream: LiveData<Bitmap> = avatarBitmapEvent

    private val messageObserver: (List<Message>) -> Unit = { newMessageList: List<Message> ->
        messages = mergeMessages(newMessageList)

        executors.main.execute {
            if (messages.isNotEmpty()) newMessagesEvent.value = buildMessageViewDataModel()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val automatedMessageSubject: BehaviorSubject<List<Message>> = BehaviorSubject(listOf())

    private val profileObserver: (Person?) -> Unit = { profile ->
        if (profile?.email?.isNotEmpty() == true) shouldCollectProfileData = false
    }

    init {
        messageManager.messages.observe(messageObserver)
        automatedMessageSubject.observe(messageObserver)
        messageManager.profile.observe(profileObserver)
        errorMessagesEvent.postValue(ValidationDataModel())
        if (hasAutomatedMessage) {
            automatedMessageSubject.value = listOf(
                Message(
                    type = Message.MESSAGE_TYPE_TEXT,
                    body = messageCenterModel.automatedMessage?.body,
                    sender = null,
                    messageStatus = Message.Status.Sending,
                    automated = true,
                    inbound = false,
                )
            )
        }
        if (!avatarUrl.isNullOrEmpty()) loadAvatar(avatarUrl)
        if (messageSLA.isNotEmpty()) onMessageCenterEvent(
            event = MessageCenterEvents.EVENT_NAME_STATUS,
            data = null
        )
    }

    override fun onCleared() {
        // Clears the observer
        messageManager.messages.removeObserver(messageObserver)
        messageManager.profile.removeObserver(profileObserver)
        automatedMessageSubject.removeObserver(messageObserver)
        super.onCleared()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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

    private fun getEmailHint() = messageCenterModel.profile?.initial?.emailHint.orEmpty()

    private fun getNameHint() = messageCenterModel.profile?.initial?.nameHint.orEmpty()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun mergeMessages(newMessages: List<Message>): List<Message> {
        val mergedMessagesList = mutableListOf<Message>()

        // If message is already in list, update it
        mergedMessagesList.addAll(
            messages.map { message ->
                newMessages.firstOrNull { it.nonce == message.nonce } ?: message
            }
        )

        // If message is not in list, add it
        mergedMessagesList.addAll(newMessages.filterNot { mergedMessagesList.contains(it) })

        // Filter out hidden messages, sort by createdAt time, then group by date
        return mergedMessagesList.filterAndGroupMessages()
    }

    private fun List<Message>.filterAndGroupMessages(): List<Message> =
        groupMessages(filterNot { it.hidden == true })

    fun exitMessageCenter() {
        onMessageCenterEvent(
            event = MessageCenterEvents.EVENT_NAME_CLOSE,
            data = mapOf("cause" to "menu_item")
        )
        exitEvent.postValue(true)
    }

    private fun getAvatar(): Bitmap? {
        if (!isAvatarLoading && avatarBitmapStream.value == null) {
            Log.d(MESSAGE_CENTER, "Fetch message center avatar image")
            isAvatarLoading = true
            avatarUrl?.let { loadAvatar(avatarUrl) }
            isAvatarLoading = false
        }
        return avatarBitmapStream.value
    }

    fun sendMessage(message: String, name: String? = null, email: String? = null) {
        // Validate profile only if the profile view is visible
        if (!isSendingMessage &&
            (
                shouldCollectProfileData && validateMessageWithProfile(message, email) ||
                    !shouldCollectProfileData && validateMessage(message)
                )
        ) {
            isSendingMessage = true
            executors.state.execute {
                shouldCollectProfileData = false
                if (hasAutomatedMessage) {
                    messages.findLast { it.automated == true }?.let {
                        messageManager.sendMessage(it)
                        hasAutomatedMessage = false
                    }
                }

                val draftAttachments = draftAttachmentsStream.value?.filter { it.hasLocalFile() }
                messageManager.sendMessage(message, draftAttachments.orEmpty())
                draftAttachmentsSubject.postValue(emptyList())
                clearMessageEvent.postValue(true)
                messageManager.updateProfile(name, email)
                isSendingMessage = false
            }
        } else {
            Log.d(MESSAGE_CENTER, "Cannot send blank message or message sending")
        }
    }

    fun onMessageCenterEvent(event: String, data: Map<String, Any?>?) {
        executors.state.execute {
            context.engage(
                event = Event.internal(event, interaction = InteractionType.MessageCenter),
                interactionId = messageCenterModel.interactionId,
                data = data
            )
        }
    }

    fun onMessageViewStatusChanged(isActive: Boolean) {
        messageManager.onMessageCenterLaunchStatusChanged(isActive)
    }

    @VisibleForTesting
    fun validateMessageWithProfile(message: String, email: String?): Boolean {
        return if (message.isBlank() && draftAttachmentsStream.value.isNullOrEmpty()) {
            val validationDataModel = ValidationDataModel(messageError = true)
            errorMessagesEvent.value = validationDataModel
            errorMessagesEvent.value = validationDataModel.copy(emailError = !validateProfile(email, messageCenterModel))
            false
        } else {
            val isValid = validateProfile(email, messageCenterModel)
            val validationDataModel = ValidationDataModel(messageError = false, emailError = !isValid)
            errorMessagesEvent.value = validationDataModel
            isValid
        }
    }

    private fun validateMessage(message: String): Boolean {
        return if (message.isBlank() && draftAttachmentsStream.value.isNullOrEmpty()) {
            errorMessagesEvent.value = ValidationDataModel(messageError = true)
            false
        } else {
            errorMessagesEvent.value = ValidationDataModel(messageError = false)
            true
        }
    }

    fun isProfileRequired(): Boolean = messageCenterModel.profile?.require == true

    private fun isProfileConfigured(): Boolean =
        messageCenterModel.profile?.request == true || messageCenterModel.profile?.require == true

    fun handleUnreadMessages() {
        if (messages.any { it.read != true }) {
            messages.filter { it.read != true }.onEach {
                if (!it.inbound) onMessageCenterEvent(
                    event = MessageCenterEvents.EVENT_NAME_READ,
                    data = mapOf(
                        "message_id" to it.id,
                        "message_type" to it.type
                    )
                )
                it.read = true
            }

            messageManager.updateMessages(messages)
        }
    }

    fun shouldHideProfileIcon() = messages.isEmpty() || hasAutomatedMessageInSending() || !isProfileConfigured()

    fun isProfileViewVisible(): Boolean = isProfileConfigured() && (messages.isEmpty() || hasAutomatedMessageInSending())

    private fun hasAutomatedMessageInSending(): Boolean = messages.size == 1 && messages[0].automated == true && messages[0].messageStatus == Message.Status.Sending

    fun buildMessageViewDataModel(): List<MessageViewData> {
        val messageViewData = mutableListOf<MessageViewData>()
        messageViewData.add(0, MessageViewData(ListItemType.HEADER, GreetingData(greeting, greetingBody, getAvatar()), null, null))
        messages.forEach { message ->
            messageViewData.add(MessageViewData(ListItemType.MESSAGE, null, null, message))
        }
        messageViewData.add(MessageViewData(ListItemType.FOOTER, null, ProfileViewData(getEmailHint(), getNameHint(), shouldCollectProfileData), null))
        return messageViewData
    }

    fun getFirstUnreadMessagePosition(adapterItems: List<MessageViewData>): Int {
        return adapterItems.indexOfFirst {
            it.message != null && it.message.read != true && !it.message.inbound
        }
    }

    data class ValidationDataModel(
        val nameError: Boolean = false,
        val emailError: Boolean = false,
        val messageError: Boolean = false
    )

    fun addAttachment(activity: Activity, uri: Uri) {
        val loadingAttachment = Message.Attachment(isLoading = true)
        var updatedAttachments = draftAttachmentsStream.value.orEmpty().plus(loadingAttachment)
        draftAttachmentsSubject.value = updatedAttachments

        executors.state.execute {
            FileUtil.createLocalStoredAttachment(activity, uri.toString(), generateUUID())?.let { file ->
                updatedAttachments = draftAttachmentsStream.value.orEmpty().minus(loadingAttachment).plus(file)
                executors.main.execute { draftAttachmentsSubject.value = updatedAttachments }
            }
        }
        onMessageCenterEvent(
            event = MessageCenterEvents.EVENT_NAME_ATTACH,
            data = null
        )
    }

    fun addAttachments(files: List<Message.Attachment>) {
        val updatedAttachments = draftAttachmentsStream.value.orEmpty().plus(files)
        draftAttachmentsSubject.value = updatedAttachments
    }

    fun removeAttachment(file: Message.Attachment) {
        val updatedAttachments = draftAttachmentsStream.value.orEmpty().minus(file)
        draftAttachmentsSubject.value = updatedAttachments
        executors.state.execute { FileUtil.deleteFile(file.localFilePath) }
        onMessageCenterEvent(
            event = MessageCenterEvents.EVENT_NAME_ATTACHMENT_DELETE,
            data = null
        )
    }

    fun downloadFile(message: Message, attachment: Message.Attachment) {
        attachment.url?.run {
            executors.state.execute {
                messageManager.downloadAttachment(context.getAppActivity(), message, attachment)
            }
        }
    }

    private fun loadAvatar(imageUrl: String) {
        executors.state.execute {
            val avatarBitmap = ImageUtil.loadAvatar(imageUrl)
            avatarBitmap?.let { avatarBitmapEvent.postValue(it) }
        }
    }
}

fun validateProfile(email: String?, model: MessageCenterModel): Boolean {
    return when {
        model.profile?.require == true && PatternsCompat.EMAIL_ADDRESS.matcher(email.toString()).matches() -> true
        model.profile?.request == true && model.profile?.require == false && email?.isEmpty() == true -> true
        model.profile?.request == true && PatternsCompat.EMAIL_ADDRESS.matcher(email.toString()).matches() -> true
        else -> false
    }
}
