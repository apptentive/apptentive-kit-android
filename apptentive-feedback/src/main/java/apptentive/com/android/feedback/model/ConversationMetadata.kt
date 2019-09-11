package apptentive.com.android.feedback.model

import java.io.File

class ConversationMetadata {
    private val items: MutableList<ConversationMetadataItem> = mutableListOf()

    fun addItem(item: ConversationMetadataItem) {
        items.add(item)
    }

    fun findItem(state: ConversationState): ConversationMetadataItem? =
        items.find { it: ConversationMetadataItem ->
            it.conversationState == state
        }
}

// TODO: make immutable
data class ConversationMetadataItem(
    val localConversationId: String,
    val dataFile: File,
    val messagesFile: File,
    var conversationState: ConversationState = ConversationState.UNDEFINED,
    var conversationId: String? = null,
    var conversationToken: String? = null,
    var conversationEncryptionKey: String? = null,
    var userId: String? = null
)