package apptentive.com.android.feedback.message

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.feedback.conversation.ConversationMetaData
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.conversation.ConversationState
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.feedback.engagement.util.MockFileSystem
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.Sender
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.feedback.utils.FileStorageUtil
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Before
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
    @Before
    override fun setUp() {
        super.setUp()
        val messageFile = createTempFile("messages.bin")
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        DependencyProvider.register<FileSystem>(MockFileSystem())
        mockkObject(FileStorageUtil)
        every {
            FileStorageUtil.getMessagesFileForActiveUser(any())
        } returns messageFile
        every {
            FileStorageUtil.getMessagesFile()
        } returns messageFile
    }

    @Test
    fun testLoadingNonExistingMessages() {
        val serializer = DefaultMessageSerializer(
            encryption = EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted
            ),
            conversationRoster = ConversationRoster()
        )
        val messages = serializer.loadMessages()
        Truth.assertThat(messages).isEmpty()
    }

    @Test
    fun testSerialization() {
        val serializer = DefaultMessageSerializer(
            encryption = EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted,
            ),
            conversationRoster = ConversationRoster(
                activeConversation = ConversationMetaData(
                    ConversationState.Undefined, tempFolder.root.path
                )
            )
        )
        serializer.saveMessages(convertToMessageEntry(testMessageList))
        val actual = serializer.loadMessages()
        Assert.assertEquals(convertToMessageEntry(testMessageList), actual)
    }

    @Test(expected = MessageSerializerException::class)
    fun testCorruptedMessageData() {
        val corruptedMessagesFile = createTempFile("corrupted-messages.bin")
        // write random data
        corruptedMessagesFile.writeBytes(Random.nextBytes(1))

        every {
            FileStorageUtil.getMessagesFile()
        } returns corruptedMessagesFile

        val serializer = DefaultMessageSerializer(
            encryption = EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted
            ),
            conversationRoster = ConversationRoster(
                activeConversation = ConversationMetaData(
                    ConversationState.Undefined, tempFolder.root.path
                )
            )
        )

        // Throws MessagesSerializerException
        serializer.loadMessages()
    }

    private fun createTempFile(name: String) = File(tempFolder.root, "${UUID.randomUUID()}-$name")
}
