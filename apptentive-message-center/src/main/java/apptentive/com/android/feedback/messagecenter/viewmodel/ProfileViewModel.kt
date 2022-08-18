package apptentive.com.android.feedback.messagecenter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.feedback.dependencyprovider.MessageCenterModelFactory
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.model.Person

class ProfileViewModel : ViewModel() {
    private val model = DependencyProvider.of<MessageCenterModelFactory>().messageCenterModel()
    private val messageManager: MessageManager = DependencyProvider.of<MessageManagerFactory>().messageManager()

    val profileTitle: String = model.profile?.edit?.title ?: ""
    val profileSubmit: String = model.profile?.edit?.saveButton ?: ""
    val nameHint: String = model.profile?.edit?.nameHint ?: ""
    val emailHint: String = model.profile?.edit?.emailHint ?: ""
    var storedName: String = ""
    var storedEmail: String = ""

    private val errorMessages = LiveEvent<Boolean>()
    val errorMessagesStream: LiveData<Boolean> = errorMessages

    private val senderProfile = LiveEvent<Person>()
    val profileStream: LiveData<Person> = senderProfile

    private val showConfirmation = LiveEvent<Boolean>()
    val showConfirmationStream: LiveData<Boolean> = showConfirmation

    private val profileObserver: (Person?) -> Unit = { profile ->
        senderProfile.postValue(profile)
        storedName = profile?.name ?: ""
        storedEmail = profile?.email ?: ""
    }

    init {
        messageManager.profile.observe(profileObserver)
    }

    override fun onCleared() {
        // Clears the observer
        messageManager.profile.removeObserver(profileObserver)
        super.onCleared()
    }

    fun submitProfile(name: String, email: String) {
        if (validateProfile(email, model)) {
            messageManager.updateProfile(name, email)
            showConfirmation.value = false
        } else {
            errorMessages.value = true
        }
    }

    fun exitProfileView(name: String, email: String) {
        showConfirmation.postValue(storedName != name || storedEmail != email)
    }
}
