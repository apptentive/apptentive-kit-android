package apptentive.com.android.feedback.utils

import androidx.annotation.WorkerThread
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import java.io.File

internal object FileStorageUtil {

    const val CONVERSATION_DIR = "conversations"

    @WorkerThread
    fun getConversationFile(): File {
        val conversationsDir = getConversationDir()
        return File(conversationsDir, "conversation.bin")
    }

    @WorkerThread
    fun getConversationFileForActiveUser(directory: String): File {
        val conversationsDir = getConversationDirForActiveUser(directory)
        return File(conversationsDir, "conversation.bin")
    }

    @WorkerThread
    fun getManifestFile(): File {
        val conversationsDir = getConversationDir()
        return File(conversationsDir, "manifest.bin")
    }

    @WorkerThread
    fun getConversationDir(): File {
        return FileUtil.getInternalDir(CONVERSATION_DIR, createIfNecessary = true)
    }

    private fun getConversationDirForActiveUser(directory: String): File {
        return FileUtil.getInternalDir(directory, createIfNecessary = true)
    }

    @WorkerThread
    fun getMessagesFile(): File {
        val conversationsDir = getConversationDir()
        return File(conversationsDir, "messages.bin")
    }

    @WorkerThread
    fun getMessagesFileForActiveUser(directory: String): File {
        val conversationsDir = getConversationDirForActiveUser(directory)
        return File(conversationsDir, "messages.bin")
    }

    @WorkerThread
    fun getRosterFile(id: String): File {
        val conversationsDir = getConversationDir()
        return File(conversationsDir, "roster${id.sha256()}.bin")
    }

    @WorkerThread
    fun getStoredMessagesFile(roster: ConversationRoster): File? {
        // Use the old messages.bin file for older SDKs < 6.2.0
        // SDK_VERSION is added in 6.1.0. It would be null for the SDKs < 6.1.0
        return if (hasStoragePriorToMultiUserSupport())
            getMessagesFile()
        else {
            Log.d(LogTags.MESSAGE_CENTER, "Setting message file from roster meta data: ${roster.activeConversation?.path}")
            roster.activeConversation?.path?.let { getMessagesFileForActiveUser(it) }
        }
    }

    @WorkerThread
    fun hasStoragePriorToMultiUserSupport(): Boolean {
        val cachedSDKVersion = DependencyProvider.of<AndroidSharedPrefDataStore>()
            .getString(SharedPrefConstants.SDK_CORE_INFO, SharedPrefConstants.SDK_VERSION).ifEmpty { null }

        return FileUtil.containsFiles(CONVERSATION_DIR) &&
            cachedSDKVersion == null || cachedSDKVersion == "6.1.0"
    }

    @WorkerThread
    fun hasStoragePriorToSkipLogic(): Boolean {
        val storedSdkVersion = DependencyProvider.of<AndroidSharedPrefDataStore>()
            .getString(SharedPrefConstants.SDK_CORE_INFO, SharedPrefConstants.SDK_VERSION).ifEmpty { null }
        return storedSdkVersion == null && FileUtil.containsFiles(CONVERSATION_DIR)
    }

    @WorkerThread
    fun deleteMessageFile() {
        Log.w(LogTags.CRYPTOGRAPHY, "Message cache is deleted to support the new encryption setting")
        val messageFile = getStoredMessagesFile(DefaultStateMachine.conversationRoster)
        val messageFilePriorToMUSupport = getMessagesFile()
        FileUtil.deleteFile(messageFilePriorToMUSupport.path)
        val currentMessageFile = getStoredMessagesFile(DefaultStateMachine.conversationRoster)
        messageFile?.let { FileUtil.deleteFile(currentMessageFile?.path) }
    }
}
