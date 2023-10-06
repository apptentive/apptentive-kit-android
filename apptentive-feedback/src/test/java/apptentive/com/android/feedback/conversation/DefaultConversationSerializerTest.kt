package apptentive.com.android.feedback.conversation

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.feedback.createMockConversation
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.feedback.engagement.util.MockFileSystem
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.feedback.utils.FileStorageUtil
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID
import kotlin.random.Random

class DefaultConversationSerializerTest : TestCase() {
    @get:Rule
    val tempFolder = TemporaryFolder()
    lateinit var conversationFile: File
    lateinit var manifestFile: File

    @Before
    override fun setUp() {
        val mockDataStore = MockAndroidSharedPrefDataStore()
        mockDataStore.putString("test", "test", "test")
        DependencyProvider.register<AndroidSharedPrefDataStore>(mockDataStore)
        DependencyProvider.register<FileSystem>(MockFileSystem())
        conversationFile = createTempFile("conversation.bin")
        manifestFile = createTempFile("manifest.bin")
        mockkObject(FileStorageUtil)
        every {
            FileStorageUtil.getManifestFile()
        } returns manifestFile
        every {
            FileStorageUtil.getConversationFileForActiveUser(any())
        } returns conversationFile
    }

    @Test
    fun testLoadingNonExistingConversation() {
        val serializer = DefaultConversationSerializer(
            conversationRosterFile = createTempFile("roster.bin"),
        ).apply {
            setEncryption(
                encryption = EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
        }
        serializer.setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        val conversation = serializer.loadConversation()
        assertThat(conversation).isNull()
    }

    @Test
    fun testSerialization() {
        every {
            FileStorageUtil.hasStoragePriorToSkipLogic()
        } returns false
        val serializer = DefaultConversationSerializer(
            conversationRosterFile = createTempFile("roster.bin"),
        ).apply {
            setEncryption(
                encryption = EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
        }
        serializer.setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        serializer.initializeSerializer()
        val conversation = createMockConversation()
        serializer.saveConversation(conversation)

        val actual = serializer.loadConversation()

        assertEquals(conversation, actual)
    }

    @Test
    fun testSerializationNewConversation() {
        val serializer = DefaultConversationSerializer(
            conversationRosterFile = createTempFile("roster.bin"),
        ).apply {
            setEncryption(
                encryption = EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
        }
        serializer.setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        serializer.initializeSerializer()
        val conversation = createMockConversation(
            engagementManifest = EngagementManifest()
        )
        serializer.saveConversation(conversation)

        val actual = serializer.loadConversation()
        assertThat(conversation).isEqualTo(actual)
    }

    @Test
    fun testSingleManifestSerialization() {
        every {
            FileStorageUtil.hasStoragePriorToSkipLogic()
        } returns false
        val manifestFile = createTempFile("manifest.json")
        val serializer = DefaultConversationSerializer(
            conversationRosterFile = createTempFile("roster.bin"),
        ).apply {
            setEncryption(
                encryption = EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
        }
        serializer.setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        serializer.initializeSerializer()
        val conversation = createMockConversation()
        serializer.saveConversation(conversation)

        // delete manifest file
        manifestFile.delete()

        // save conversation one more time
        serializer.saveConversation(conversation)

        // manifest file should not be re-created
        assertFalse(manifestFile.exists())

        // update engagement manifest
        val newConversation = conversation.copy(
            engagementManifest = conversation.engagementManifest.copy(expiry = 2000.0)
        )
        serializer.saveConversation(newConversation)

        // successfully load the manifest
        val actual = serializer.loadConversation()
        assertThat(newConversation).isEqualTo(actual)
    }

    @Test(expected = ConversationSerializationException::class)
    fun testCorruptedConversationData() {
        every {
            FileStorageUtil.hasStoragePriorToSkipLogic()
        } returns true // write random data
        conversationFile.writeBytes(Random.nextBytes(10))

        val serializer = DefaultConversationSerializer(
            conversationRosterFile = createTempFile("roster.bin"),
        ).apply {
            setEncryption(
                encryption = EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
        }
        serializer.setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        serializer.saveRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        serializer.initializeSerializer()

        // this throws an exception
        serializer.loadConversation()
    }

    @Test
    fun testCorruptedManifestData() {
        every {
            FileStorageUtil.hasStoragePriorToSkipLogic()
        } returns true
        val manifestFile = createTempFile("manifest.json")
        val serializer = DefaultConversationSerializer(
            conversationRosterFile = createTempFile("roster.bin"),
        ).apply {
            setEncryption(
                encryption = EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
            setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
            initializeSerializer()
        }

        val conversation = createMockConversation()
        serializer.setRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))
        serializer.saveConversation(conversation,)

        // write corrupted data
        manifestFile.writeText("{") // illegal json

        // this should still load a valid conversation
        val actual = serializer.loadConversation()
        assertThat(actual).isNotNull()

        // manifest would just be set to "default" and re-fetched next time
        val expected = createMockConversation(
            engagementManifest = EngagementManifest()
        )

        assertThat(actual).isEqualTo(expected)
    }

    private fun createTempFile(name: String) = File(tempFolder.root, "${UUID.randomUUID()}-$name")
}
