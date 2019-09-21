package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.backend.ConversationFetchService
import apptentive.com.android.feedback.backend.ConversationTokenFetchBody
import apptentive.com.android.feedback.model.*
import apptentive.com.android.feedback.model.ConversationState.ANONYMOUS_PENDING
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.util.Factory
import apptentive.com.android.util.Log
import apptentive.com.android.util.generateUUID
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

class ConversationManager(
    private val conversationSerializer: ConversationSerializer,
    private val appReleaseFactory: Factory<AppRelease>,
    private val personFactory: Factory<Person>,
    private val deviceFactory: Factory<Device>,
    private val sdkFactory: Factory<SDK>,
    private val conversationFetchService: ConversationFetchService
) {
    private val conversation: Conversation? = null

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    private fun loadConversation() {
        try {
            val conversation = loadActiveConversation()
            if (conversation?.state == ANONYMOUS_PENDING) {
                fetchConversationToken(conversation)
            }
        } catch (e: Exception) {

        }
    }

    private fun fetchConversationToken(conversation: Conversation) {
        val request = ConversationTokenFetchBody.from(
            device = conversation.device,
            sdk = conversation.sdk,
            appRelease = conversation.appRelease
        )
        TODO("Fetch conversation token")
    }

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    private fun loadActiveConversation(): Conversation? {
        val existingConversation = conversationSerializer.loadConversation()
        if (existingConversation != null) {
            return existingConversation
        }

        // no active conversations: create a new one
        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return Conversation(
            localIdentifier = generateConversationIdentifier(),
            state = ANONYMOUS_PENDING,
            person = personFactory.create(),
            device = deviceFactory.create(),
            appRelease = appReleaseFactory.create(),
            sdk = sdkFactory.create()
        )
    }

    companion object {
        private fun generateConversationIdentifier() = generateUUID()
    }
}

interface ConversationSerializer {
    @Throws(ConversationSerializationException::class)
    fun loadConversation(): Conversation?

    @Throws(ConversationSerializationException::class)
    fun saveConversation(conversation: Conversation)
}

internal class SingleFileConversationSerializer(
    private val file: File
) : ConversationSerializer {
    override fun saveConversation(conversation: Conversation) {
        file.outputStream().use { stream ->
            val encoder = BinaryEncoder(DataOutputStream(stream))
            encoder.encodeConversation(conversation)
        }
    }

    override fun loadConversation(): Conversation? {
        return if (!file.exists()) null else
            file.inputStream().use { stream ->
                val encoder = BinaryDecoder(DataInputStream(stream))
                return@loadConversation encoder.decodeConversation()
            }
    }
}