package apptentive.com.android.feedback.messagecenter.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.core.util.PatternsCompat
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
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.utils.convertToGroupDate
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

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
    var messages: List<Message> = getMessagesFromManager().filterSortAndGroupMessages()
    var showLauncherView: Boolean = messages.isEmpty() && showProfile()

    private val newMessagesSubject = LiveEvent<List<Message>>()
    val newMessages: LiveData<List<Message>> get() = newMessagesSubject

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

    private val profileObserver: (Person?) -> Unit = { profile ->
        if (profile?.email?.isNotEmpty() == true) showLauncherView = false
    }

    init {
        messageManager.messages.observe(messageObserver)
        messageManager.profile.observe(profileObserver)
        errorMessages.postValue(ValidationDataModel())
    }

    override fun onCleared() {
        // Clears the observer
        messageManager.messages.removeObserver(messageObserver)
        messageManager.profile.removeObserver(profileObserver)
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

    fun getEmailHint() = model.profile?.initial?.emailHint

    fun getNameHint() = model.profile?.initial?.nameHint

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
            clearMessage.postValue(true)
            messageManager.sendMessage(message)
            messageManager.updateProfile(name, email)
        } else {
            Log.d(LogTags.MESSAGE_CENTER, "Cannot send blank message")
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
        // TODO verify with attachments
        return if (message.isBlank()) {
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
        // TODO verify with attachments
        return if (message.isBlank()) {
            errorMessages.value = ValidationDataModel(messageError = true)
            false
        } else {
            errorMessages.value = ValidationDataModel(messageError = false)
            true
        }
    }

    fun showProfile(): Boolean =
        model.profile?.request == true || model.profile?.require == true

    data class ValidationDataModel(
        val nameError: Boolean = false,
        val emailError: Boolean = false,
        val messageError: Boolean = false
    )
}

fun validateProfile(email: String?, model: MessageCenterModel): Boolean {
    return when {
        model.profile?.require == true && PatternsCompat.EMAIL_ADDRESS.matcher(email.toString()).matches() -> true
        model.profile?.request == true && model.profile?.require == false && email?.isEmpty() == true -> true
        model.profile?.request == true && PatternsCompat.EMAIL_ADDRESS.matcher(email.toString()).matches() -> true
        else -> false
    }
}
