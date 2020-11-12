package apptentive.com.android.feedback.conversation

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.createMockConversation
import apptentive.com.android.feedback.model.EngagementManifest
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertFalse
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.*
import kotlin.random.Random

class DefaultConversationSerializerTest : TestCase() {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testLoadingNonExistingConversation() {
        val conversationFile = createTempFile("conversation.bin")
        val serializer = DefaultConversationSerializer(
            conversationFile = conversationFile,
            manifestFile = createTempFile("manifest.json")
        )

        val conversation = serializer.loadConversation()
        assertThat(conversation).isNull()
    }

    @Test
    fun testSerialization() {
        val serializer = DefaultConversationSerializer(
            conversationFile = createTempFile("conversation.bin"),
            manifestFile = createTempFile("manifest.json")
        )

        val conversation = createMockConversation()
        serializer.saveConversation(conversation)

        val actual = serializer.loadConversation()
        assertThat(conversation).isEqualTo(actual)
    }

    @Test
    fun testSerializationNewConversation() {
        val serializer = DefaultConversationSerializer(
            conversationFile = createTempFile("conversation.bin"),
            manifestFile = createTempFile("manifest.json")
        )

        val conversation = createMockConversation(
            engagementManifest = EngagementManifest()
        )
        serializer.saveConversation(conversation)

        val actual = serializer.loadConversation()
        assertThat(conversation).isEqualTo(actual)
    }

    @Test
    fun testSingleManifestSerialization() {
        val manifestFile = createTempFile("manifest.json")
        val serializer = DefaultConversationSerializer(
            conversationFile = createTempFile("conversation.bin"),
            manifestFile = manifestFile
        )

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
    fun testCorruptedData() {
        val conversationFile = createTempFile("conversation.bin")
        // write random data
        conversationFile.writeBytes(Random.nextBytes(10))

        val manifestFile = createTempFile("manifest.json")
        val serializer = DefaultConversationSerializer(
            conversationFile = conversationFile,
            manifestFile = manifestFile
        )

        // this throws an exception
        serializer.loadConversation()
    }

    @Test
    @Ignore
    fun testCorruptedManifest()
    {
        
    }

    private fun createTempFile(name: String) = File(tempFolder.root, "${UUID.randomUUID()}-${name}")
}