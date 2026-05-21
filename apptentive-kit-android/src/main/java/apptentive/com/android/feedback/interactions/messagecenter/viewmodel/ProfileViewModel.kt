package apptentive.com.android.feedback.interactions.messagecenter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LogTags.MESSAGE_CENTER
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.interactions.messagecenter.dependencyprovider.MessageCenterModelFactory
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.model.MessageCenterModel
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getEngagementContextOrNull
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getMessageManager
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.ui.core.LiveEvent
import apptentive.com.android.util.Log

internal class ProfileViewModel : ViewModel() {

    val dismissActivity: LiveEvent<Unit> = LiveEvent()
    private val messageManager: MessageManager? = try {
        getMessageManager()
    } catch (e: Exception) {
        Log.e(MESSAGE_CENTER, "Provider is not registered, could not create MessageManager", e)
        dismissActivity.setValue(Unit)
        null
    }

    private val model: MessageCenterModel = try {
        DependencyProvider.of<MessageCenterModelFactory>().messageCenterModel()
    } catch (e: Exception) {
        getInteractionBackup()
    }

    val profileTitle: String = model.profile?.edit?.title ?: ""
    val profileSubmit: String = model.profile?.edit?.saveButton ?: ""
    val nameHint: String = model.profile?.edit?.nameHint ?: ""
    val emailHint: String = model.profile?.edit?.emailHint ?: ""
    private var storedName: String = ""
    private var storedEmail: String = ""

    private val errorMessages = LiveEvent<Boolean>()
    val errorMessagesStream: LiveData<Boolean> = errorMessages

    private val senderProfile = LiveEvent<Person?>()
    val profileStream: LiveEvent<Person?> = senderProfile

    private val showConfirmation = LiveEvent<Boolean>()
    val showConfirmationStream: LiveData<Boolean> = showConfirmation

    private val profileObserver: (Person?) -> Unit = { profile ->
        senderProfile.postValue(profile)
        storedName = profile?.name ?: ""
        storedEmail = profile?.email ?: ""
    }

    init {
        messageManager?.profile?.observe(profileObserver)
    }

    override fun onCleared() {
        // Clears the observer
        messageManager?.profile?.removeObserver(profileObserver)
        super.onCleared()
    }

    fun submitProfile(name: String, email: String) {
        if (validateProfile(email, model)) {
            messageManager?.updateProfile(name, email)
            showConfirmation.value = false
        } else {
            errorMessages.value = true
        }
    }

    fun isProfileRequired(): Boolean = model.profile?.require == true

    fun onMessageCenterEvent(event: String, data: Map<String, Any?>?) {
        getEngagementContextOrNull(MESSAGE_CENTER)?.let { context ->
            context.executors.state.execute {
                context.engage(
                    event = Event.internal(event, interaction = InteractionType.MessageCenter),
                    interactionId = model.interactionId,
                    data = data
                )
            }
        }
    }

    fun exitProfileView(name: String, email: String) {
        showConfirmation.postValue(storedName != name || storedEmail != email)
    }
}
