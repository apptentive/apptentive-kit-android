package apptentive.com.android.feedback.conversation

import androidx.core.util.AtomicFile
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.conversation.DefaultSerializers.conversationSerializer
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream

internal interface ConversationSerializer {
    @Throws(ConversationSerializationException::class)
    fun loadConversation(): Conversation?

    @Throws(ConversationSerializationException::class)
    fun saveConversation(conversation: Conversation)

    fun setEncryption(encryption: Encryption)
}

internal class DefaultConversationSerializer(
    private val conversationFile: File,
    private val manifestFile: File,
) : ConversationSerializer {

    private lateinit var encryption: Encryption

    // we keep track of the last seen engagement manifest expiry date and only update storage if it changes
    private var lastKnownManifestExpiry: TimeInterval = 0.0

    override fun saveConversation(conversation: Conversation) {
        val start = System.currentTimeMillis()
        val atomicFile = AtomicFile(conversationFile)
        val stream = atomicFile.startWrite()
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            val encoder = BinaryEncoder(DataOutputStream(byteArrayOutputStream))
            conversationSerializer.encode(encoder, conversation)
            val encryptedBytes = encryption.encrypt(byteArrayOutputStream.toByteArray())
            stream.use {
                stream.write(encryptedBytes)
                atomicFile.finishWrite(stream)
            }
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            throw ConversationSerializationException("Unable to save conversation", e)
        }

        Log.v(CONVERSATION, "Conversation data saved (took ${System.currentTimeMillis() - start} ms)")

        val newExpiry = conversation.engagementManifest.expiry
        if (lastKnownManifestExpiry != newExpiry) {
            val json = JsonConverter.toJson(conversation.engagementManifest)
            val atomicManifestFile = AtomicFile(manifestFile)
            val manifestStream = atomicManifestFile.startWrite()
            try {
                manifestStream.use {
                    manifestStream.write(json.toByteArray())
                    atomicManifestFile.finishWrite(manifestStream)
                }
            } catch (e: Exception) {
                atomicManifestFile.failWrite(manifestStream)
                throw ConversationSerializationException("Unable to save engagement manifest", e)
            }
            lastKnownManifestExpiry = newExpiry
        }
    }

    @Throws(ConversationSerializationException::class)
    override fun loadConversation(): Conversation? {
        if (conversationFile.exists()) {
            val conversation = readConversation()

            // Added in 6.1.0. Previous versions will be `null`.
            val storedSdkVersion = DependencyProvider.of<AndroidSharedPrefDataStore>()
                .getString(SharedPrefConstants.SDK_CORE_INFO, SharedPrefConstants.SDK_VERSION).ifEmpty { null }

            val engagementManifest = if (storedSdkVersion != null) readEngagementManifest() else null
            if (engagementManifest != null) {
                return conversation.copy(engagementManifest = engagementManifest)
            }

            return conversation
        }

        return null
    }

    override fun setEncryption(encryption: Encryption) {
        this.encryption = encryption
    }

    private fun readConversation(): Conversation =
        try {
            val decryptedMessage = encryption.decrypt(FileInputStream(conversationFile))
            val inputStream = ByteArrayInputStream(decryptedMessage)
            val decoder = BinaryDecoder(DataInputStream(inputStream))
            conversationSerializer.decode((decoder))
        } catch (e: Exception) {
            throw ConversationSerializationException("Unable to load conversation", e)
        }

    private fun readEngagementManifest(): EngagementManifest? {
        try {
            if (manifestFile.exists()) {
                val json = manifestFile.readText()
                return JsonConverter.fromJson(json)
            }
        } catch (e: Exception) {
            Log.e(CONVERSATION, "Unable to load engagement manifest: $manifestFile", e)
        }
        return null
    }
}
