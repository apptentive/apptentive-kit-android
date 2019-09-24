package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.backend.ConversationFetchService
import apptentive.com.android.feedback.backend.ConversationTokenFetchBody
import apptentive.com.android.feedback.backend.ConversationTokenFetchResponse
import apptentive.com.android.feedback.model.*
import apptentive.com.android.feedback.model.ConversationState.ANONYMOUS
import apptentive.com.android.feedback.model.ConversationState.ANONYMOUS_PENDING
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.util.Callback
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
    private var _activeConversation: Conversation? = null // TODO: should it really be nullable?

    private var activeConversation: Conversation?
        get() = _activeConversation
        set(conversation) {
            _activeConversation = conversation
            if (conversation != null) {
                try {
                    conversationSerializer.saveConversation(conversation)
                } catch (e: Exception) {
                    Log.e(CONVERSATION, "Exception while saving conversation")
                }
            }
        }

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    fun loadConversation() {
        try {
            activeConversation = loadActiveConversation()
            activeConversation?.let { conversation ->
                if (conversation.state == ANONYMOUS_PENDING) {
                    fetchConversationToken(conversation)
                }
            }
        } catch (e: Exception) {
            TODO()
        }
    }

    private fun fetchConversationToken(conversation: Conversation) {
        val request = ConversationTokenFetchBody.from(
            device = conversation.device,
            sdk = conversation.sdk,
            appRelease = conversation.appRelease
        )
        conversationFetchService.fetchConversationToken(
            request,
            object : Callback<ConversationTokenFetchResponse> {
                override fun onComplete(response: ConversationTokenFetchResponse) {
                    val currentConversation = activeConversation

                    // TODO: extract a helper function which would check request consistency
                    if (currentConversation == null) {
                        Log.d(
                            CONVERSATION,
                            "Active conversation became inactive while fetch conversation token request was fetching."
                        )
                        return
                    }

                    if (currentConversation.localIdentifier != conversation.localIdentifier) {
                        Log.d(
                            CONVERSATION,
                            "Conversation fetch token request was created for a conversation with local ID '${conversation.localIdentifier}' but active conversation has local ID '${currentConversation.localIdentifier}'"
                        )
                        return
                    }
                    if (currentConversation.state != ANONYMOUS_PENDING) {
                        Log.d(
                            CONVERSATION,
                            "Conversation fetch token request should only affect conversations with state ${ConversationState.ANONYMOUS_PENDING} but active conversation has state ${currentConversation.state}"
                        )
                        return
                    }

                    activeConversation = currentConversation.copy(
                        state = ANONYMOUS,
                        conversationToken = response.token,
                        conversationId = response.id,
                        person = currentConversation.person.copy(
                            id = response.person_id
                        )
                    )
                }

                override fun onFailure(t: Throwable) {
                }
            })
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