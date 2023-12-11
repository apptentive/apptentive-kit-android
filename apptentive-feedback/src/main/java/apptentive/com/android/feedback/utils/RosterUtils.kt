package apptentive.com.android.feedback.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.encryption.EncryptionNoOp
import apptentive.com.android.feedback.conversation.ConversationMetaData
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.conversation.ConversationSerializationException
import apptentive.com.android.feedback.conversation.ConversationState
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.utils.AndroidSDKVersion.getSDKVersion
import apptentive.com.android.feedback.utils.ThrottleUtils.ROSTER_TYPE
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.generateUUID

internal object RosterUtils {
    private val conversationRepository by lazy {
        DependencyProvider.of<ConversationRepository>()
    }

    private val messageRepository by lazy {
        DependencyProvider.of<MessageRepository>()
    }

    @Throws(ConversationSerializationException::class)
    fun initializeRoster() {
        try {
            val conversationRoster = conversationRepository.initializeRepositoryWithRoster()

            val loggedInState =
                conversationRoster.activeConversation?.state as? ConversationState.LoggedIn

            loggedInState?.let { updateEncryptionForLoggedInConversation(it) }
            DefaultStateMachine.conversationRoster = conversationRoster
            conversationRepository.updateConversationRoster(conversationRoster)
        } catch (e: ConversationSerializationException) {
            if (!ThrottleUtils.shouldThrottleReset(ROSTER_TYPE)) {
                Log.e(LogTags.CONVERSATION, "Cannot load existing roster", e)
                Log.d(LogTags.CONVERSATION, "Deserialization failure, deleting the conversation files")
                FileUtil.deleteUnrecoverableStorageFiles(FileUtil.getInternalDir("conversations"))
            } else {
                throw ConversationSerializationException(
                    "Cannot load existing roster, roster reset throttled",
                    e
                )
            }
        }
    }

    fun mergeLegacyRoster(legacyRoster: ConversationRoster) {
        val currentRoster = DefaultStateMachine.conversationRoster
        currentRoster.activeConversation = legacyRoster.activeConversation
        currentRoster.loggedOut = legacyRoster.loggedOut + currentRoster.loggedOut
        currentRoster.activeConversation?.let {
            if (it.state is ConversationState.LoggedIn) {
                updateEncryptionForLoggedInConversation(it.state as ConversationState.LoggedIn)
            }
        }
        DefaultStateMachine.conversationRoster = currentRoster
        conversationRepository.updateConversationRoster(currentRoster)
    }

    private fun updateEncryptionForLoggedInConversation(loggedInState: ConversationState.LoggedIn) {
        if (!isMarshmallowOrGreater()) return

        val wrapperEncryptionBytes = loggedInState.encryptionWrapperBytes
        val encryptionKey = wrapperEncryptionBytes.getEncryptionKey(loggedInState.subject)
        DefaultStateMachine.encryption = AESEncryption23(encryptionKey)
        conversationRepository.updateEncryption(DefaultStateMachine.encryption)
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
        conversationRepository.saveRoster(conversationRoster)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateRosterForLogin(subject: String, encryptionKey: EncryptionKey, wrapperEncryption: ByteArray) {
        if (getSDKVersion() < Build.VERSION_CODES.M) return
        val conversationRoster = DefaultStateMachine.conversationRoster
        val activeConversationMetaData = conversationRoster.activeConversation
        val loggedOut = conversationRoster.loggedOut.toMutableList()
        val loggedInConversation = ConversationMetaData(ConversationState.LoggedIn(subject, wrapperEncryption), "")
        val matchingLoggedOutConversation = findAndRemoveMatchingLoggedOutConversation(loggedOut, subject)

        when {
            // Previous state was anonymous
            activeConversationMetaData != null && activeConversationMetaData.state is ConversationState.Anonymous -> {
                conversationRoster.activeConversation = loggedInConversation.copy(path = activeConversationMetaData.path)
            }
            // Previous state was logged out && not in legacy format. No active conversation state
            activeConversationMetaData == null && matchingLoggedOutConversation != null && !FileUtil.isConversationCacheStoredInLegacyFormat(matchingLoggedOutConversation.path) -> {
                conversationRoster.activeConversation = loggedInConversation.copy(path = matchingLoggedOutConversation.path)
            }
            // Previous state was logged in. It is a session restart now
            activeConversationMetaData != null && activeConversationMetaData.state is ConversationState.LoggedIn -> {
                conversationRoster.activeConversation = loggedInConversation.copy(path = activeConversationMetaData.path)
            }
            // New login conversation or previous state was logged out and in LEGACY format.
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

    private fun findAndRemoveMatchingLoggedOutConversation(loggedOut: MutableList<ConversationMetaData>, subject: String): ConversationMetaData? {
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

    fun getActiveConversationMetaData(): ConversationMetaData? {
        return DefaultStateMachine.conversationRoster.activeConversation
    }

    fun hasNoConversationCache(): Boolean {
        return (DefaultStateMachine.conversationRoster.activeConversation == null || DefaultStateMachine.conversationRoster.activeConversation?.state is ConversationState.Undefined) &&
            DefaultStateMachine.conversationRoster.loggedOut.isEmpty()
    }
}

internal object AndroidSDKVersion {
    fun getSDKVersion(): Int {
        return Build.VERSION.SDK_INT
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
internal fun isMarshmallowOrGreater(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
