package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.core.MutableObservable
import apptentive.com.android.core.Observable
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.model.*
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.util.Factory
import apptentive.com.android.util.Log
import apptentive.com.android.util.Result
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
    private val conversationService: ConversationService
) {
    private val _activeConversation: MutableObservable<Conversation>
    val activeConversation: Observable<Conversation> get() = _activeConversation

    private val _engagementManifest = MutableObservable(EngagementManifest())
    val engagementManifest: Observable<EngagementManifest> = _engagementManifest

    init {
        val conversation = loadActiveConversation()
        _activeConversation = MutableObservable(conversation)
        _activeConversation.observe(::saveConversation)
        _activeConversation.observe(::tryFetchEngagementManifest)

        // fetch conversation token if necessary
        if (!conversation.hasConversationToken) {
            fetchConversationToken(conversation)
        }
    }

    private fun fetchConversationToken(conversation: Conversation) {
        conversationService.fetchConversationToken(
            device = conversation.device,
            sdk = conversation.sdk,
            appRelease = conversation.appRelease
        ) {
            when (it) {
                is Result.Error -> Log.e(CONVERSATION, "Unable to fetch conversation")
                is Result.Success -> {
                    val currentConversation = _activeConversation.value
                    @Suppress("CascadeIf")
                    if (currentConversation.localIdentifier != conversation.localIdentifier) {
                        Log.d(
                            CONVERSATION,
                            "Conversation fetch token request was created for a conversation with local ID '${conversation.localIdentifier}' but active conversation has local ID '${currentConversation.localIdentifier}'"
                        )
                    } else if (currentConversation.hasConversationToken) {
                        Log.d(
                            CONVERSATION,
                            "Conversation fetch token request should only affect conversations without token but active conversation has token ${currentConversation.conversationToken}"
                        )
                    } else {
                        _activeConversation.value = currentConversation.copy(
                            conversationToken = it.data.token,
                            conversationId = it.data.id,
                            person = currentConversation.person.copy(
                                id = it.data.person_id
                            )
                        )
                    }
                }
            }
        }
    }

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    private fun loadActiveConversation(): Conversation {
        val existingConversation = conversationSerializer.loadConversation()
        if (existingConversation != null) {
            return existingConversation
        }

        // no active conversations: create a new one
        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return Conversation(
            localIdentifier = generateConversationIdentifier(),
            person = personFactory.create(),
            device = deviceFactory.create(),
            appRelease = appReleaseFactory.create(),
            sdk = sdkFactory.create()
        )
    }

    @WorkerThread
    private fun saveConversation(conversation: Conversation) {
        try {
            conversationSerializer.saveConversation(conversation)
        } catch (exception: Exception) {
            Log.e(CONVERSATION, "Exception while saving conversation")
        }
    }

    @WorkerThread
    private fun tryFetchEngagementManifest(conversation: Conversation) {
        val token = conversation.conversationToken
        val id = conversation.conversationId
        if (token != null && id != null) {
            conversationService.fetchEngagementManifest(
                conversationToken = token,
                conversationId = id
            ) {
                TODO()
            }
        }
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