package apptentive.com.android.feedback.message

import apptentive.com.android.TestCase
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID
import kotlin.random.Random

class DefaultMessageSerializerTest : TestCase() {
    @get:Rule
    val tempFolder = TemporaryFolder()

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
    fun testLoadingNonExistingMessages() {
        val serializer = DefaultMessageSerializer(
            messagesFile = createTempFile("messages.bin"),
            encryption = EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted
            )
        )
        val messages = serializer.loadMessages()
        Truth.assertThat(messages).isEmpty()
    }

    @Test
    fun testSerialization() {
        val serializer = DefaultMessageSerializer(
            messagesFile = createTempFile("messages.bin"),
            encryption = EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted,
            )
        )
        serializer.saveMessages(convertToMessageEntry(testMessageList))
        val actual = serializer.loadMessages()
        Assert.assertEquals(convertToMessageEntry(testMessageList), actual)
    }

    @Test(expected = MessageSerializerException::class)
    fun testCorruptedMessageData() {
        val messagesFile = createTempFile("messages.bin")
        // write random data
        messagesFile.writeBytes(Random.nextBytes(1))

        val serializer = DefaultMessageSerializer(
            messagesFile = messagesFile,
            encryption = EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted
            )
        )

        // Throws MessagesSerializerException
        serializer.loadMessages()
    }

    private fun createTempFile(name: String) = File(tempFolder.root, "${UUID.randomUUID()}-$name")
}
