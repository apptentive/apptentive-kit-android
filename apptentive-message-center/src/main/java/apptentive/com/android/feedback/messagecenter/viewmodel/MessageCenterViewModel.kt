package apptentive.com.android.feedback.messagecenter.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
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
import apptentive.com.android.feedback.messagecenter.view.MessageViewData
import apptentive.com.android.feedback.messagecenter.view.ProfileViewData
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageCenterModel
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.utils.FileUtil
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
    val messageSLA: String = model.status?.body ?: ""
    var messages: List<Message> = getMessagesFromManager().filterSortAndGroupMessages()
    var hasAutomatedMessage: Boolean = !model.automatedMessage?.body.isNullOrEmpty()
    var showLauncherView: Boolean = messages.isEmpty() && showProfile()

    private val newMessagesSubject = LiveEvent<List<Message>>()
    val newMessages: LiveData<List<Message>> get() = newMessagesSubject

    private val draftAttachmentsEvent = LiveEvent<List<Message.Attachment>>()
    val draftAttachmentsStream: LiveData<List<Message.Attachment>> = draftAttachmentsEvent

    private val attachmentDownloadQueueEvent = LiveEvent<Set<Message.Attachment>>()
    val attachmentDownloadQueueStream: LiveData<Set<Message.Attachment>> = attachmentDownloadQueueEvent

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val clearMessage = LiveEvent<Boolean>()
    val clearMessageStream: LiveData<Boolean> = clearMessage

    private val errorMessages = LiveEvent<ValidationDataModel>()
    val errorMessagesStream: LiveData<ValidationDataModel> = errorMessages

    private val messageObserver: (List<Message>) -> Unit = { newMessageList: List<Message> ->
        messages = mergeMessages(newMessageList)

        executors.main.execute {
            if (messages.isNotEmpty()) newMessagesSubject.value = messages
        }
    }

    private val automatedMessageSubject: BehaviorSubject<List<Message>> = BehaviorSubject(listOf())

    private val profileObserver: (Person?) -> Unit = { profile ->
        if (profile?.email?.isNotEmpty() == true) showLauncherView = false
    }

    init {
        messageManager.messages.observe(messageObserver)
        automatedMessageSubject.observe(messageObserver)
        messageManager.profile.observe(profileObserver)
        errorMessages.postValue(ValidationDataModel())
        if (hasAutomatedMessage) {
            automatedMessageSubject.value = listOf(
                Message(
                    type = Message.MESSAGE_TYPE_TEXT,
                    body = model.automatedMessage?.body,
                    sender = null,
                    messageStatus = Message.Status.Sending,
                    automated = true,
                    inbound = false,
                )
            )
        }
    }

    override fun onCleared() {
        // Clears the observer
        messageManager.messages.removeObserver(messageObserver)
        messageManager.profile.removeObserver(profileObserver)
        automatedMessageSubject.removeObserver(messageObserver)
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

    private fun getEmailHint() = model.profile?.initial?.emailHint.orEmpty()

    private fun getNameHint() = model.profile?.initial?.nameHint.orEmpty()

    private fun mergeMessages(newMessages: List<Message>): List<Message> {
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
        return mergedMessagesList.filterSortAndGroupMessages()
    }

    private fun getMessagesFromManager(): List<Message> = messageManager.getAllMessages()

    private fun List<Message>.filterSortAndGroupMessages(): List<Message> =
        groupMessages(filterNot { it.hidden == true }.sortedBy { it.createdAt })

    fun exitMessageCenter() {
        onMessageCenterEvent(MessageCenterEvents.EVENT_NAME_CLOSE)
        exitEvent.postValue(true)
    }

    fun sendMessage(message: String, name: String? = null, email: String? = null) {
        // Validate profile only if the profile view is visible
        if (showLauncherView && validateMessageWithProfile(message, email) ||
            !showLauncherView && validateMessage(message)
        ) {
            showLauncherView = false
            if (hasAutomatedMessage) {
                messages.findLast { it.automated == true }?.let {
                    messageManager.sendMessage(it)
                    hasAutomatedMessage = false
                }
            }

            messageManager.sendMessage(message, draftAttachmentsStream.value.orEmpty())
            draftAttachmentsEvent.postValue(emptyList())
            clearMessage.postValue(true)
            messageManager.updateProfile(name, email)
        } else {
            Log.d(MESSAGE_CENTER, "Cannot send blank message")
        }
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

    @VisibleForTesting
    fun validateMessageWithProfile(message: String, email: String?): Boolean {
        return if (message.isBlank() && draftAttachmentsStream.value.isNullOrEmpty()) {
            val validationDataModel = ValidationDataModel(messageError = true)
            errorMessages.value = validationDataModel
            errorMessages.value = validationDataModel.copy(emailError = !validateProfile(email, model))
            false
        } else {
            val isValid = validateProfile(email, model)
            val validationDataModel = ValidationDataModel(messageError = false, emailError = !isValid)
            errorMessages.value = validationDataModel
            isValid
        }
    }

    private fun validateMessage(message: String): Boolean {
        return if (message.isBlank() && draftAttachmentsStream.value.isNullOrEmpty()) {
            errorMessages.value = ValidationDataModel(messageError = true)
            false
        } else {
            errorMessages.value = ValidationDataModel(messageError = false)
            true
        }
    }

    fun showProfile(): Boolean =
        model.profile?.request == true || model.profile?.require == true

    fun buildMessageViewDataModel(profileVisibility: Boolean = true): List<MessageViewData> {
        val messageViewData = mutableListOf<MessageViewData>()
        messageViewData.add(0, MessageViewData(GreetingData(greeting, greetingBody), null, null))
        messages.map { message ->
            messageViewData.add(MessageViewData(null, null, message))
        }
        messageViewData.add(MessageViewData(null, ProfileViewData(getEmailHint(), getNameHint(), profileVisibility), null))
        return messageViewData
    }

    data class ValidationDataModel(
        val nameError: Boolean = false,
        val emailError: Boolean = false,
        val messageError: Boolean = false
    )

    fun addAttachment(activity: Activity, uri: Uri) {
        FileUtil.createLocalStoredAttachment(activity, uri.toString(), generateUUID())?.let { file ->
            val updatedAttachments = draftAttachmentsStream.value.orEmpty().plus(file)
            draftAttachmentsEvent.value = updatedAttachments
        }
    }

    fun addAttachments(files: List<Message.Attachment>) {
        val updatedAttachments = draftAttachmentsStream.value.orEmpty().plus(files)
        draftAttachmentsEvent.value = updatedAttachments
    }

    fun removeAttachment(file: Message.Attachment) {
        val updatedAttachments = draftAttachmentsStream.value.orEmpty().minus(file)
        draftAttachmentsEvent.value = updatedAttachments
        FileUtil.deleteFile(file.localFilePath)
    }

    fun downloadFile(message: Message, file: Message.Attachment) {
        file.url?.run {
            val downloadQueueStartDownload = attachmentDownloadQueueStream.value.orEmpty().plus(file)
            attachmentDownloadQueueEvent.value = downloadQueueStartDownload

            messageManager.downloadAttachment(context.getAppActivity(), message, file) {
                val downloadQueueFinishDownload = attachmentDownloadQueueStream.value.orEmpty().minus(file)
                attachmentDownloadQueueEvent.postValue(downloadQueueFinishDownload)
            }
        }
    }

    fun isFileDownloading(file: Message.Attachment): Boolean {
        return attachmentDownloadQueueStream.value?.contains(file) == true
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
