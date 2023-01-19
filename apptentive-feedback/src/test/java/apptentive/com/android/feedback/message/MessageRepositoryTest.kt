package apptentive.com.android.feedback.message

import android.text.format.DateUtils.DAY_IN_MILLIS
import apptentive.com.android.TestCase
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Ignore
import org.junit.Test

class MessageRepositoryTest : TestCase() {

    private val attachments = listOf(
        Message.Attachment(
            id = "attachment 1",
            contentType = "jpg",
            size = 100000,
            url = "www.google.com",
            sourceUriOrPath = null,
            localFilePath = "cache://attachment1",
            originalName = "attachment1.jpg"
        ),
        Message.Attachment(
            id = "attachment 2",
            contentType = "png",
            size = 10000,
            url = null,
            sourceUriOrPath = "content://attachment2",
            localFilePath = "cache://attachment2",
            originalName = "attachment2.jpg"
        )
    )

    private val testMessageList: List<Message> = listOf(
        Message(
            id = "Test",
            nonce = "UUID",
            type = "MC",
            body = "Hello",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis()),
            attachments = attachments
        ),
        Message(
            id = "Test2",
            nonce = "UUID2",
            type = "MC2",
            body = "Hello2",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis() - DAY_IN_MILLIS)
        ),
        Message(
            id = "Test3",
            nonce = "UUID3",
            type = "MC3",
            body = "Hello3",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS + 100))
        ),
        Message(
            id = "Test4",
            nonce = "UUID4",
            type = "MC4",
            body = "Hello4",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 7)),
            attachments = attachments.subList(0, 0)
        ),
        Message(
            id = "Test5",
            nonce = "UUID5",
            type = "MC5",
            body = "Hello5",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 365))
        ),
        Message(
            id = "Test6 Hidden",
            nonce = "UUID6",
            type = "MC6",
            body = "Hello6",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis() - 1000),
            hidden = true
        )
    )

    @Test
    fun testAddMessages() {
        val messageRepo = DefaultMessageRepository(MockMessageSerializer(testMessageList))
        messageRepo.addOrUpdateMessages(testMessageList)
        addResult(testMessageList.sortedBy { it.createdAt })
        assertResults(messageRepo.getAllMessages())
    }

    @Test
    fun testUpdateMessage() {
        val updatedMessage = Message(
            id = "Test",
            nonce = "UUID",
            type = "MC",
            body = "Hi",
            sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
        )
        val messageRepository = DefaultMessageRepository(MockMessageSerializer(testMessageList))

        // Testing update message
        messageRepository.addOrUpdateMessages(listOf(updatedMessage))
        assertEquals(
            messageRepository.getAllMessages().find { it.nonce == updatedMessage.nonce },
            updatedMessage
        )
    }

    @Test
    @Ignore("Ignoring for now because flaky when run on Jenkins")
    fun testAddOrUpdateMessage() {
        val messageRepository = DefaultMessageRepository(MockMessageSerializer(testMessageList))

        val updatedMessage = Message(
            id = "Test updated",
            nonce = "UUID",
            type = "MC updated",
            messageStatus = Message.Status.Sent,
            body = "body updated",
            sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
            attachments = listOf(
                Message.Attachment(
                    id = "attachment 1",
                    contentType = "png",
                    size = 1000,
                    url = "www.newurl.com",
                    sourceUriOrPath = "saved://path",
                    localFilePath = "cache://attachment1234",
                    originalName = "attachment123.jpg"
                ),
                Message.Attachment(
                    id = "attachment 2",
                    contentType = "jpeg",
                    size = 100000,
                    url = "urlnow.com",
                    sourceUriOrPath = "content://attachment234",
                    localFilePath = "cache://attachment234",
                    originalName = "attachment255.jpg"
                )
            )
        )

        val newMessage = Message(
            id = "Test",
            nonce = "UUID8",
            type = "MC",
            body = "Message added",
            sender = Sender(id = "1234", name = "John Doe", profilePhoto = null),
        )

        val newList = listOf(updatedMessage, newMessage)
        val listToUpdate = listOf(updatedMessage.copy(), newMessage.copy()) // Don't edit existing

        messageRepository.addOrUpdateMessages(listToUpdate)

        newList.forEach { message ->
            val before = testMessageList.find { it.nonce == message.nonce }
            val saved = messageRepository.getAllMessages().find { it.nonce == message.nonce }
            assertEquals(saved?.id, message.id)
            assertEquals(saved?.nonce, message.nonce)
            assertEquals(saved?.groupTimestamp, message.groupTimestamp)
            assertEquals(saved?.automated, message.automated)
            assertEquals(saved?.body, message.body)
            assertEquals(saved?.customData, message.customData)
            assertEquals(saved?.hidden, message.hidden)
            assertEquals(saved?.inbound, message.inbound)
            assertEquals(saved?.read, message.read)
            assertEquals(saved?.sender, message.sender)
            assertEquals(saved?.type, message.type)
            assertEquals(saved?.messageStatus, message.messageStatus)

            if (message.nonce == "UUID") { // Updated
                assertEquals(saved?.createdAt, message.createdAt)
                assertNotEquals(before?.createdAt, message.createdAt) // Updated
                assertNotEquals(saved?.messageStatus, before?.messageStatus)
            } else assertEquals(saved?.createdAt, message.createdAt)

            message.attachments?.forEach { attach ->
                val savedAttach = saved?.attachments?.find { it.id == attach.id }
                assertEquals(savedAttach?.contentType, attach.contentType)
                assertEquals(savedAttach?.localFilePath, attach.localFilePath)
                assertEquals(savedAttach?.url, attach.url)
                assertEquals(savedAttach?.originalName, attach.originalName)
                assertEquals(savedAttach?.size, attach.size)

                // These should not change and are not as important so we don't update (should we?)
                assertNotEquals(savedAttach?.creationTime, attach.creationTime)
                assertNotEquals(savedAttach?.sourceUriOrPath, attach.sourceUriOrPath)
            }
        }
    }

    @Test
    fun deleteMessage() {
        val messageRepository = DefaultMessageRepository(MockMessageSerializer(listOf()))
        messageRepository.addOrUpdateMessages(testMessageList)
        messageRepository.deleteMessage("UUID")
        val expectedList = testMessageList.toMutableList().apply {
            removeAt(0)
            sortBy { it.createdAt }
        }
        assertEquals(expectedList, messageRepository.getAllMessages())
    }

    @Test
    fun testLastReceivedMessageID() {
        val messageRepository = DefaultMessageRepository(MockMessageSerializer(testMessageList))
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
        messageRepository.addOrUpdateMessages(messages)
        addResult("Test2")
        assertResults(messageRepository.getLastReceivedMessageIDFromEntries())
    }

    private class MockMessageSerializer(testList: List<Message>) : MessageSerializer {
        var savedList: List<DefaultMessageRepository.MessageEntry> = convertToMessageEntry(testList)

        override fun loadMessages(): List<DefaultMessageRepository.MessageEntry> {
            return savedList
        }

        override fun saveMessages(messages: List<DefaultMessageRepository.MessageEntry>) {
            savedList = messages
        }

        override fun deleteAllMessages() {
        }
    }
}

internal fun convertToMessageEntry(messages: List<Message>): List<DefaultMessageRepository.MessageEntry> {
    val messageEntries = mutableListOf<DefaultMessageRepository.MessageEntry>()
    for (message in messages) {
        val newEntry = DefaultMessageRepository.MessageEntry(
            id = message.id,
            messageState = message.messageStatus.name,
            createdAt = message.createdAt,
            nonce = message.nonce,
            messageJson = JsonConverter.toJson(message)
        )
        messageEntries.add(newEntry)
    }
    return messageEntries
}
