package apptentive.com.android.feedback.conversation

import androidx.core.util.AtomicFile
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.conversation.DefaultSerializers.conversationRosterSerializer
import apptentive.com.android.feedback.conversation.DefaultSerializers.conversationSerializer
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.utils.FileStorageUtils
import apptentive.com.android.feedback.utils.FileStorageUtils.hasStoragePriorToMultiUserSupport
import apptentive.com.android.feedback.utils.FileStorageUtils.hasStoragePriorToSkipLogic
import apptentive.com.android.feedback.utils.FileUtil
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
    fun saveConversation(conversation: Conversation)

    fun setEncryption(encryption: Encryption)

    fun setRoster(conversationRoster: ConversationRoster)

    fun saveRoster(conversationRoster: ConversationRoster)
}

internal class DefaultConversationSerializer(
    private val conversationRosterFile: File,
) : ConversationSerializer {

    private lateinit var encryption: Encryption

    private lateinit var conversationRoster: ConversationRoster

    private lateinit var manifestFile: File

    // we keep track of the last seen engagement manifest expiry date and only update storage if it changes
    private var lastKnownManifestExpiry: TimeInterval = 0.0

    @Throws(ConversationSerializationException::class)
    override fun saveConversation(conversation: Conversation) {
        val rosterConversationFile = getConversationFileFromRoster(conversationRoster)
            ?: throw ConversationLoggedOutException("No active conversation metadata found, unable to save conversation", null)
        saveRoster(conversationRoster)
        val start = System.currentTimeMillis()
        val atomicFile = AtomicFile(rosterConversationFile)
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
        val conversationFile = loadConversationFile(conversationRoster)
        if (conversationFile?.exists() == true) {
            val conversation = readConversation(conversationFile)

            if (hasStoragePriorToMultiUserSupport()) {
                // Transfer the entries to new conversation file
                saveConversation(conversation)
                // Delete old conversation.bin if exists
                FileUtil.deleteFile(conversationFile.path)
            }

            // By setting engagementManifest null it will be refreshed for the users returning from < v6.1.0
            val engagementManifest = if (hasStoragePriorToSkipLogic()) readEngagementManifest() else null
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

    override fun setRoster(conversationRoster: ConversationRoster) {
        this.conversationRoster = conversationRoster
        saveRoster(conversationRoster)
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
            ConversationRoster(activeConversation = ConversationMetaData(ConversationState.Undefined, path = path))
        }
        return conversationRoster
    }

    override fun saveRoster(conversationRoster: ConversationRoster) {
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

    private fun readConversation(rosterConversationFile: File): Conversation =
        try {
            val decryptedMessage = encryption.decrypt(FileInputStream(rosterConversationFile))
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

    private fun getConversationFileFromRoster(roster: ConversationRoster): File? {
        Log.d(CONVERSATION, "Setting conversation file from roster: $roster")
        return roster.activeConversation?.let { activeConversation ->
            Log.d(CONVERSATION, "Using conversation file: ${activeConversation.path}")
            FileStorageUtils.getConversationFileForActiveUser(activeConversation.path)
        }
    }

    private fun loadConversationFile(conversationRoster: ConversationRoster?): File? {
        // Use the old messages.bin file for older SDKs < 6.2.0
        // SDK_VERSION is added in 6.1.0. It would be null for the SDKs < 6.1.0
        return if (hasStoragePriorToMultiUserSupport()) {
            Log.d(CONVERSATION, "Using old conversation file")
            FileStorageUtils.getConversationFile()
        } else {
            conversationRoster?.let { roster ->
                getConversationFileFromRoster(roster)
            }
        }
    }
}
