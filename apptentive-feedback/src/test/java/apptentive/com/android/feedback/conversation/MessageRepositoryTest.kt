package apptentive.com.android.feedback.conversation

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.message.DefaultMessageRepository
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import org.junit.Test

class MessageRepositoryTest : TestCase() {

    private val testMessageList: List<Message> = listOf(
        Message(
            id = "Test",
            nonce = "UUID",
            type = "MC",
            body = "Hello",
            sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
        )
    )

    @Test
    fun testAddMessages() {
        val messageRepo = DefaultMessageRepository()
        messageRepo.addOrUpdateMessage(testMessageList)
        addResult(testMessageList)
        assertResults(messageRepo.getAllMessages())
    }

    @Test
    fun testUpdateMessage() {
        val messageRepository = DefaultMessageRepository()
        messageRepository.addOrUpdateMessage(testMessageList)
        val updatedMessage = Message(
            id = "Test",
            nonce = "UUID",
            type = "MC",
            body = "Hi",
            sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
        )
        // Testing update message
        messageRepository.updateMessage(updatedMessage)
        addResult(listOf(updatedMessage))
        assertResults(messageRepository.getAllMessages())

        // Testing add or update message
        val updatedList = listOf(
            Message(
                id = "Test",
                nonce = "UUID",
                type = "MC",
                body = "body updated",
                sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
            ),
            Message(
                id = "Test",
                nonce = "UUID2",
                type = "MC",
                body = "Message added",
                sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
            )
        )

        addResult(updatedList)
        messageRepository.addOrUpdateMessage(updatedList)
    }

    @Test
    fun deleteMessage() {
        val messageRepository = DefaultMessageRepository()
        messageRepository.addOrUpdateMessage(testMessageList)
        messageRepository.deleteMessage("UUID")
        addResult(listOf<Message>())
        assertResults(messageRepository.getAllMessages())
    }

    @Test
    fun testLastReceivedMessageID() {
        val messageRepository = DefaultMessageRepository()
        val messages = listOf(
            Message(
                id = "Test1",
                nonce = "UUID",
                type = "MC",
                body = "body updated",
                messageStatus = Message.Status.Saved,
                sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
            ),
            Message(
                id = "Test2",
                nonce = "UUID2",
                type = "MC",
                body = "Message added",
                messageStatus = Message.Status.Saved,
                sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
            )
        )
        messageRepository.addOrUpdateMessage(messages)
        addResult("Test2")
        assertResults(messageRepository.getLastReceivedMessageIDFromEntries())
    }
}
