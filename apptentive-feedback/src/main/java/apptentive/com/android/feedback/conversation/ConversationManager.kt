package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.ConversationMetadata
import apptentive.com.android.feedback.model.ConversationMetadataItem
import apptentive.com.android.feedback.model.ConversationState
import apptentive.com.android.feedback.model.ConversationState.*
import apptentive.com.android.feedback.utils.CoroutineLauncher
import apptentive.com.android.feedback.utils.LogTag.CONVERSATION
import apptentive.com.android.feedback.utils.logi
import java.io.File
import java.util.*

class ConversationManager(
    private val conversationDir: File,
    private val launcher: CoroutineLauncher
) {
    @Throws(ConversationLoadingException::class)
    private suspend fun loadActiveConversation(): Conversation? {
        val metadata = loadMetadata(conversationDir)

        val existingConversation = loadConversation(metadata)
        if (existingConversation != null) {
            return existingConversation
        }

        // no active conversations: create a new one
        logi(CONVERSATION, "Creating 'anonymous' conversation...")
        val dataFile = generateConversationDataFilename(conversationDir)
        val messagesFile = generateMessagesFilename(conversationDir)
        return Conversation(
            localIdentifier = generateConversationIdentifier(),
            dataFile = dataFile,
            messagesFile = messagesFile,
            state = ConversationState.ANONYMOUS_PENDING
        )
    }

    //region Metadata

    @WorkerThread
    private fun loadMetadata(conversationDir: File): ConversationMetadata {
        val metadataFile = File(conversationDir, CONVERSATION_METADATA_FILE)
        if (metadataFile.exists()) {
            TODO("Implement me")
        }

        return ConversationMetadata()
    }

    //endregion

    //region Conversation

    private fun loadConversation(metadata: ConversationMetadata): Conversation? {
        // we're going to scan metadata in attempt to find existing conversations
        // if the user was logged in previously - we should have an active conversation
        var item = metadata.findItem(LOGGED_IN)
        if (item != null) {
            logi(CONVERSATION, "Loading 'logged-in' conversation...")
            return loadConversation(item)
        }

        // if no users were logged in previously - we might have an anonymous conversation
        item = metadata.findItem(ANONYMOUS)
        if (item != null) {
            logi(CONVERSATION, "Loading 'anonymous' conversation...")
            return loadConversation(item)
        }

        // check if we have a 'pending' anonymous conversation
        item = metadata.findItem(ANONYMOUS_PENDING)
        if (item != null) {
            logi(CONVERSATION, "Loading 'anonymous pending' conversation...")
            return loadConversation(item)
        }

        // check if we have a 'legacy pending' conversation
//        item = metadata.findItem(LEGACY_PENDING)
//        if (item != null) {
//            logi(CONVERSATION, "Loading 'legacy pending' conversation...")
//            val conversation = loadConversation(item!!)
//            fetchLegacyConversation(conversation)
//            return conversation
//        }

        // we only have LOGGED_OUT conversations
        logi(
            CONVERSATION,
            "No active conversations to load: only 'logged-out' conversations available"
        )
        return null
    }

    private fun loadConversation(item: ConversationMetadataItem): Conversation {
        TODO()
    }

    //endregion

    companion object {
        private const val CONVERSATION_METADATA_FILE = "conversation-v2.meta"

        private fun generateMessagesFilename(conversationDir: File): File {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        private fun generateConversationDataFilename(conversationDir: File): File {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        private fun generateConversationIdentifier() = UUID.randomUUID().toString()
    }
}

internal interface ConversationLoader {
    @Throws(ConversationLoadingException::class)
    suspend fun loadConversation(): Conversation?
}

private class MetadataConversationLoader(
    private val metadataFile: File
) : ConversationLoader {
    override suspend fun loadConversation(): Conversation? {
        TODO()
    }
}