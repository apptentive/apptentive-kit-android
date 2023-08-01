package apptentive.com.android.feedback.utils

import androidx.annotation.WorkerThread
import java.io.File

internal object FileStorageUtils {

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
        return File(conversationsDir, "roster$id.bin")
    }
}
