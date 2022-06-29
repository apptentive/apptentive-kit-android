package apptentive.com.android.feedback.message

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER

@InternalUseOnly
interface MessageRepository {
    fun addOrUpdateMessage(messages: List<Message>)
    fun updateMessage(message: Message)
    fun getAllMessages(): List<Message>
    fun getLastReceivedMessageIDFromEntries(): String
    fun deleteMessage(nonce: String)
    fun saveMessages()
}

internal class DefaultMessageRepository : MessageRepository {

    private val messageEntries: MutableList<MessageEntry> = mutableListOf()

    override fun getLastReceivedMessageIDFromEntries(): String {
        return messageEntries.lastOrNull {
            it.messageState == Message.Status.Saved.name
        }?.id ?: ""
    }

    private fun findEntry(nonce: String) = messageEntries.find { it.nonce == nonce }

    private fun buildMessageFromJson(json: String): Message =
        JsonConverter.fromJson(json)

    override fun addOrUpdateMessage(messages: List<Message>) {
        for (message in messages) {
            val existing = findEntry(message.nonce)
            if (existing != null) {
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
    }

    override fun getAllMessages(): List<Message> {
        // TODO Build the messageEntries from the persistent message storage file
        // TODO Works only for Text message type, need small refactor for the messages with attachments
        val messageList = mutableListOf<Message>()
        for (entry in messageEntries) {
            val message = buildMessageFromJson(entry.messageJson)
            message.messageStatus = Message.Status.parse(entry.messageState)
            messageList.add(message)
        }
        return messageList
    }

    override fun updateMessage(message: Message) {
        val existing = findEntry(message.nonce)
        if (existing != null) {
            existing.id = message.id
            existing.messageState = message.messageStatus.name
            existing.messageJson = JsonConverter.toJson(message)
        } else {
            Log.d(MESSAGE_CENTER, "Cannot update message. Message with nonce ${message.nonce} not found.")
        }
    }

    override fun saveMessages() {
        // TODO serialize into a file
    }

    override fun deleteMessage(nonce: String) {
        val entry = messageEntries.filter { it.nonce == nonce }
        if (entry != null) messageEntries.removeAll(entry)
        else Log.d(MESSAGE_CENTER, "Cannot delete message. Message with nonce $nonce not found.")
    }

    data class MessageEntry(
        var id: String?,
        var createdAt: TimeInterval,
        var nonce: String,
        var messageState: String,
        var messageJson: String
    )
}
