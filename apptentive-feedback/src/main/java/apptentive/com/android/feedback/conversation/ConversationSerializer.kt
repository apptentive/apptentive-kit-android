package apptentive.com.android.feedback.conversation

import androidx.core.util.AtomicFile
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.conversation.DefaultSerializers.conversationRosterSerializer
import apptentive.com.android.feedback.conversation.DefaultSerializers.conversationSerializer
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.utils.FileStorageUtils
import apptentive.com.android.feedback.utils.FileStorageUtils.hasStoragePriorToMultiUserSupport
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.generateUUID
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
    fun initializeSerializer(): ConversationRoster

    @Throws(ConversationSerializationException::class)
    fun saveConversation(conversation: Conversation, roster: ConversationRoster)

    fun setEncryption(encryption: Encryption)
}

internal class DefaultConversationSerializer(
    private val conversationRosterFile: File,
) : ConversationSerializer {

    private lateinit var encryption: Encryption

    private lateinit var conversationFile: File

    private lateinit var manifestFile: File

    // we keep track of the last seen engagement manifest expiry date and only update storage if it changes
    private var lastKnownManifestExpiry: TimeInterval = 0.0

    override fun saveConversation(conversation: Conversation, roster: ConversationRoster) {
        setConversationFileFromRoster(roster)
        saveRoster(roster)
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

    internal fun saveRoster(conversationRoster: ConversationRoster) {

//        if (this.conversationRoster == conversationRoster) {
//            return
//        }
//        if (!conversationRosterFile.exists()) {
//            return
//        }

        Log.d(CONVERSATION, "Saving conversation roster: $conversationRoster")
        val atomicFile = AtomicFile(conversationRosterFile)
        val stream = atomicFile.startWrite()
        try {
            val encoder = BinaryEncoder(DataOutputStream(stream))
            conversationRosterSerializer.encode(encoder, conversationRoster)
            atomicFile.finishWrite(stream)
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            throw ConversationSerializationException("Unable to save conversation roster", e)
        }
    }

    override fun initializeSerializer(): ConversationRoster {
        // TODO verify manifest can be common for all conversations
        manifestFile = FileStorageUtils.getManifestFile()

        Log.d(CONVERSATION, "Initializing conversation serializer, manifest file: $manifestFile")

        val conversationRoster = if (conversationRosterFile.exists() && conversationRosterFile.length() > 0) {
            Log.d(CONVERSATION, "Conversation roster file exists, loading roster")
            readConversationRoster()
        } else {
            // Create conversation file
            val path = "conversations/${generateUUID()}"
            Log.d(CONVERSATION, "Conversation roster file does not exist, creating new conversation at $path")
            // TODO save conversation roster, so can compare and decide if the roster should be cached in the save conversation method
            ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, path = path))
        }
        setConversationFile(conversationRoster)
        return conversationRoster
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

    private fun readConversationRoster(): ConversationRoster =
        try {
            conversationRosterFile.inputStream().use { stream ->
                val decoder = BinaryDecoder(DataInputStream(stream))
                conversationRosterSerializer.decode(decoder)
            }
        } catch (e: Exception) {
            throw ConversationSerializationException("Unable to load conversation roster", e)
            // TODO add throttle logic like we have it for conversation
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

    private fun setConversationFileFromRoster(roster: ConversationRoster) {
        Log.d(CONVERSATION, "Setting conversation file from roster: $roster")
        roster.activeConversation?.let { activeConversation ->
            conversationFile = FileStorageUtils.getConversationFileForActiveUser(activeConversation.path)
            Log.d(CONVERSATION, "Using conversation file: $conversationFile")
        }
    }

    private fun setConversationFile(conversationRoster: ConversationRoster?) {
        // Use the old messages.bin file for older SDKs < 6.2.0
        // SDK_VERSION is added in 6.1.0. It would be null for the SDKs < 6.1.0
        if (hasStoragePriorToMultiUserSupport()) {
            conversationFile = FileStorageUtils.getConversationFile()
            Log.d(CONVERSATION, "Using old conversation file, conversationFile: $conversationFile")
        } else {
            conversationRoster?.let { roster ->
                setConversationFileFromRoster(roster)
            }
        }
    }
}
