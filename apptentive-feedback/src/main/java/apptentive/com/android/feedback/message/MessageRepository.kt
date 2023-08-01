package apptentive.com.android.feedback.message

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.generateUUID
import java.io.File

@InternalUseOnly
interface MessageRepository {
    fun addOrUpdateMessages(messages: List<Message>, conversationRoster: ConversationRoster)
    fun getAllMessages(): List<Message>
    fun getLastReceivedMessageIDFromEntries(): String
    fun deleteMessage(nonce: String)
    fun saveMessages(conversationRoster: ConversationRoster)

    fun setMessageFile(file: File)
}

internal class DefaultMessageRepository(
    val messageSerializer: MessageSerializer,
    val conversationRoster: ConversationRoster
) : MessageRepository {

    private val messageEntries: MutableList<MessageEntry> = messageSerializer.loadMessages(conversationRoster).toMutableList()

    override fun getLastReceivedMessageIDFromEntries(): String {
        return messageEntries.lastOrNull {
            it.messageState == Message.Status.Saved.name
        }?.id ?: ""
    }

    private fun findEntry(nonce: String) = messageEntries.find { it.nonce == nonce }

    private fun buildMessageFromJson(json: String): Message =
        JsonConverter.fromJson(json)

    override fun addOrUpdateMessages(messages: List<Message>, conversationRoster: ConversationRoster) {
        for (message in messages) {
            val existing = findEntry(message.nonce)
            if (existing != null) {
                val existingMessage = JsonConverter.fromJson<Message>(existing.messageJson)
                message.attachments = existingMessage.attachments?.onEach { attachment ->
                    message.attachments?.find { it.id == attachment.id }?.run {
                        // Update important values
                        if (!contentType.isNullOrBlank()) attachment.contentType = contentType
                        if (!localFilePath.isNullOrBlank()) attachment.localFilePath = localFilePath
                        if (!url.isNullOrBlank()) attachment.url = url
                        if (!originalName.isNullOrBlank()) attachment.originalName = originalName
                        if (size != 0L) attachment.size = size
                        attachment.isLoading = isLoading
                    }
                }
                message.read = existingMessage.read ?: message.read // If already set (true), keep
                existing.id = message.id
                existing.messageState = message.messageStatus.name
                existing.messageJson = JsonConverter.toJson(message)
            } else {
                message.attachments?.onEach { it.id = it.id ?: generateUUID() }
                val newEntry = MessageEntry(
                    id = message.id,
                    messageState = message.messageStatus.name,
                    createdAt = message.createdAt,
                    nonce = message.nonce,
                    messageJson = JsonConverter.toJson(message)
                )
                messageEntries.add(newEntry)
            }
        }
        saveMessages(conversationRoster)
    }

    override fun getAllMessages(): List<Message> {
        val messageList = mutableListOf<Message>()
        try {
            for (entry in messageSerializer.loadMessages(conversationRoster)) {
                val message = buildMessageFromJson(entry.messageJson)
                message.messageStatus = Message.Status.parse(entry.messageState)
                messageList.add(message)
                messageList.sortedBy { it.createdAt }
            }
        } catch (e: MessageSerializerException) {
            Log.e(MESSAGE_CENTER, "There was an exception while deserializing the messages ${e.message}")
        }
        return messageList
    }

    override fun saveMessages(conversationRoster: ConversationRoster) {
        try {
            messageSerializer.saveMessages(messages = messageEntries.sortedBy { it.createdAt }, conversationRoster)
        } catch (e: MessageSerializerException) {
            Log.e(MESSAGE_CENTER, "Cannot save messages. A Serialization issue occurred ${e.message}")
        }
    }

    override fun setMessageFile(file: File) {
        messageSerializer.setMessageFile(file)
    }

    override fun deleteMessage(nonce: String) {
        val entry = messageEntries.filter { it.nonce == nonce }
        if (entry.isNotEmpty()) {
            messageEntries.removeAll(entry)
            saveMessages(conversationRoster)
        } else Log.d(MESSAGE_CENTER, "Cannot delete message. Message with nonce $nonce not found.")
    }

    data class MessageEntry(
        var id: String?,
        var createdAt: TimeInterval,
        var nonce: String,
        var messageState: String,
        var messageJson: String
    )
}
