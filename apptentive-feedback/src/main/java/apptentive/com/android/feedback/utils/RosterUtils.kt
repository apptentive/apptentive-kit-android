package apptentive.com.android.feedback.utils

import android.os.Build
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.encryption.EncryptionNoOp
import apptentive.com.android.feedback.conversation.ConversationMetaData
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.conversation.ConversationState
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.util.generateUUID

internal object RosterUtils {
    private val conversationRepository by lazy {
        DependencyProvider.of<ConversationRepository>()
    }

    private val messageRepository by lazy {
        DependencyProvider.of<MessageRepository>()
    }

    fun initializeRoster(): ConversationRoster {
        val conversationRoster = conversationRepository.initializeRepositoryWithRoster()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            conversationRoster.activeConversation?.state is ConversationState.LoggedIn
        ) {
            val encryptionKey =
                (conversationRoster.activeConversation?.state as ConversationState.LoggedIn).encryptionKey
            DefaultStateMachine.encryption = AESEncryption23(encryptionKey)
            conversationRepository.updateEncryption(DefaultStateMachine.encryption)
        }
        conversationRepository.updateConversationRoster(conversationRoster)
        return conversationRoster
    }

    fun updateRosterForLogout(conversationId: String) {
        val conversationRoster = DefaultStateMachine.conversationRoster
        val activeConversationMetaData = conversationRoster.activeConversation
        val loggedOut = conversationRoster.loggedOut.toMutableList()
        activeConversationMetaData?.let {
            val convertedMetaData = ConversationMetaData(
                ConversationState.LoggedOut(
                    conversationId,
                    (it.state as ConversationState.LoggedIn).subject
                ),
                it.path
            )
            loggedOut.add(convertedMetaData)
        }
        conversationRoster.activeConversation = null
        conversationRoster.loggedOut = loggedOut
        updateRepositories(conversationRoster, EncryptionNoOp())
    }

    fun updateRosterForLogin(subject: String, encryptionKey: EncryptionKey) {
        val conversationRoster = DefaultStateMachine.conversationRoster
        val loggedOut = conversationRoster.loggedOut.toMutableList()
        val loggedInConversation = ConversationMetaData(ConversationState.LoggedIn(subject, encryptionKey), "")
        val activeConversationMetaData = conversationRoster.activeConversation
        val loggedInConversationFromLogout = removeFromLoggedOut(loggedOut, subject)

        when {
            activeConversationMetaData != null && activeConversationMetaData.state is ConversationState.Anonymous -> {
                conversationRoster.activeConversation = loggedInConversation.copy(path = activeConversationMetaData.path)
            }
            activeConversationMetaData == null && loggedInConversationFromLogout != null -> {
                conversationRoster.activeConversation = loggedInConversation.copy(path = loggedInConversationFromLogout.path)
            }
            else -> {
                conversationRoster.activeConversation =
                    loggedInConversation.copy(path = "conversations/${generateUUID()}")
            }
        }

        val encryption = AESEncryption23(encryptionKey)
        conversationRoster.loggedOut = loggedOut
        DefaultStateMachine.encryption = encryption
        DefaultStateMachine.conversationRoster = conversationRoster
        updateRepositories(conversationRoster, encryption)
        conversationRepository.saveRoster(conversationRoster)
    }

    private fun removeFromLoggedOut(loggedOut: MutableList<ConversationMetaData>, subject: String): ConversationMetaData? {
        val conversationToRemove = loggedOut.firstOrNull {
            it.state is ConversationState.LoggedOut && (it.state as ConversationState.LoggedOut).subject == subject
        }
        return if (conversationToRemove != null) {
            loggedOut.remove(conversationToRemove)
            conversationToRemove
        } else {
            null
        }
    }

    private fun updateRepositories(conversationRoster: ConversationRoster, encryption: Encryption) {
        DefaultStateMachine.conversationRoster = conversationRoster
        conversationRepository.updateConversationRoster(conversationRoster)
        conversationRepository.updateEncryption(encryption)
        messageRepository.updateConversationRoster(conversationRoster)
        messageRepository.updateEncryption(encryption)
    }
}

internal object AndroidSDKVersion {
    fun getSDKVersion(): Int {
        return Build.VERSION.SDK_INT
    }
}

fun getActiveConversationMetaData(): ConversationMetaData? {
    return DefaultStateMachine.conversationRoster.activeConversation
}
