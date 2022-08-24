package apptentive.com.android.feedback.message

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER

@InternalUseOnly
interface MessageRepository {
    fun addOrUpdateMessages(messages: List<Message>)
    fun updateMessage(message: Message)
    fun getAllMessages(): List<Message>
    fun getLastReceivedMessageIDFromEntries(): String
    fun deleteMessage(nonce: String)
    fun saveMessages()
}

internal class DefaultMessageRepository(val messageSerializer: MessageSerializer) : MessageRepository {

    private val messageEntries: MutableList<MessageEntry> = messageSerializer.loadMessages().toMutableList()

    override fun getLastReceivedMessageIDFromEntries(): String {
        return messageEntries.lastOrNull {
            it.messageState == Message.Status.Saved.name
        }?.id ?: ""
    }

    private fun findEntry(nonce: String) = messageEntries.find { it.nonce == nonce }

    private fun buildMessageFromJson(json: String): Message =
        JsonConverter.fromJson(json)

    override fun addOrUpdateMessages(messages: List<Message>) {
        for (message in messages) {
            val existing = findEntry(message.nonce)
            if (existing != null) {
                val existingMessage = JsonConverter.fromJson<Message>(existing.messageJson)
                message.createdAt = existingMessage.createdAt
                message.storedFiles = existingMessage.storedFiles
                existing.id = message.id
                existing.messageState = message.messageStatus.name
                existing.messageJson = JsonConverter.toJson(message)
            } else {
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
        saveMessages()
    }

    override fun getAllMessages(): List<Message> {
        val messageList = mutableListOf<Message>()
        try {
            for (entry in messageSerializer.loadMessages()) {
                val message = buildMessageFromJson(entry.messageJson)
                message.messageStatus = Message.Status.parse(entry.messageState)
                messageList.add(message)
            }
        } catch (e: MessageSerializerException) {
            Log.e(MESSAGE_CENTER, "There was an exception while deserializing the messages ${e.message}")
        }
        return messageList
    }

    override fun updateMessage(message: Message) {
        val existing = findEntry(message.nonce)
        if (existing != null) {
            existing.id = message.id
            existing.messageState = message.messageStatus.name
            existing.messageJson = JsonConverter.toJson(message)
            saveMessages()
        } else {
            Log.d(MESSAGE_CENTER, "Cannot update message. Message with nonce ${message.nonce} not found.")
        }
    }

    override fun saveMessages() {
        try {
            messageSerializer.saveMessages(messages = messageEntries)
        } catch (e: MessageSerializerException) {
            Log.e(MESSAGE_CENTER, "Cannot save messages. A Serialization issue occurred ${e.message}")
        }
    }

    override fun deleteMessage(nonce: String) {
        val entry = messageEntries.filter { it.nonce == nonce }
        if (entry.isNotEmpty()) {
            messageEntries.removeAll(entry)
            saveMessages()
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
