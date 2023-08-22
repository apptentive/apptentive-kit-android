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
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID
import kotlin.random.Random

@Ignore("TODO: Fix this test before MUA release.")
class DefaultConversationSerializerTest : TestCase() {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    override fun setUp() {
        val mockDataStore = MockAndroidSharedPrefDataStore()
        mockDataStore.putString("test", "test", "test")
        DependencyProvider.register<AndroidSharedPrefDataStore>(mockDataStore)
        DependencyProvider.register<FileSystem>(MockFileSystem())
    }

    @Test
    fun testLoadingNonExistingConversation() {
        val conversationFile = createTempFile("conversation.bin")
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

        val conversation = serializer.loadConversation(ConversationRoster())
        assertThat(conversation).isNull()
    }

    @Test
    fun testSerialization() {
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

        val conversation = createMockConversation()
        serializer.saveConversation(conversation, ConversationRoster())

        val actual = serializer.loadConversation(ConversationRoster())

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

        val conversation = createMockConversation(
            engagementManifest = EngagementManifest()
        )
        serializer.saveConversation(conversation, ConversationRoster())

        val actual = serializer.loadConversation(ConversationRoster())
        assertThat(conversation).isEqualTo(actual)
    }

    @Test
    fun testSingleManifestSerialization() {
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

        val conversation = createMockConversation()
        serializer.saveConversation(conversation, ConversationRoster())

        // delete manifest file
        manifestFile.delete()

        // save conversation one more time
        serializer.saveConversation(conversation, ConversationRoster())

        // manifest file should not be re-created
        assertFalse(manifestFile.exists())

        // update engagement manifest
        val newConversation = conversation.copy(
            engagementManifest = conversation.engagementManifest.copy(expiry = 2000.0)
        )
        serializer.saveConversation(newConversation, ConversationRoster())

        // successfully load the manifest
        val actual = serializer.loadConversation(ConversationRoster())
        assertThat(newConversation).isEqualTo(actual)
    }

    @Test(expected = ConversationSerializationException::class)
    fun testCorruptedConversationData() {
        val conversationFile = createTempFile("conversation.bin")
        // write random data
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

        serializer.saveRoster(ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))

        serializer.initializeSerializer()

        // this throws an exception
        serializer.loadConversation(ConversationRoster())
    }

    @Test
    fun testCorruptedManifestData() {
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

        val conversation = createMockConversation()
        serializer.saveConversation(conversation, ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, tempFolder.root.path)))

        // write corrupted data
        manifestFile.writeText("{") // illegal json

        // this should still load a valid conversation
        val actual = serializer.loadConversation(ConversationRoster())
        assertThat(actual).isNotNull()

        // manifest would just be set to "default" and re-fetched next time
        val expected = createMockConversation(
            engagementManifest = EngagementManifest()
        )

        assertThat(actual).isEqualTo(expected)
    }

    private fun createTempFile(name: String) = File(tempFolder.root, "${UUID.randomUUID()}-$name")
}
