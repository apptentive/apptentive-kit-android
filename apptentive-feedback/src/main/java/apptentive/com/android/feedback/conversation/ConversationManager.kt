package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.core.MutableObservable
import apptentive.com.android.core.Observable
import apptentive.com.android.core.isInThePast
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.hasConversationToken
import apptentive.com.android.util.Log
import apptentive.com.android.util.Result

class ConversationManager(
    private val conversationRepository: ConversationRepository,
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
        val existingConversation = conversationRepository.loadConversation()
        if (existingConversation != null) {
            return existingConversation
        }

        // no active conversations: create a new one
        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return conversationRepository.createConversation()
    }

    @WorkerThread
    private fun saveConversation(conversation: Conversation) {
        try {
            conversationRepository.saveConversation(conversation)
        } catch (exception: Exception) {
            Log.e(CONVERSATION, "Exception while saving conversation")
        }
    }

    @WorkerThread
    private fun tryFetchEngagementManifest(conversation: Conversation) {
        val manifest = conversation.engagementManifest
        if (!isInThePast(manifest.expiry)) {
            Log.d(CONVERSATION, "Engagement manifest up to date")
            return
        }

        val token = conversation.conversationToken
        val id = conversation.conversationId
        if (token != null && id != null) {
            conversationService.fetchEngagementManifest(
                conversationToken = token,
                conversationId = id
            ) {
                when (it) {
                    is Result.Success -> {
                        _activeConversation.value = _activeConversation.value.copy(
                            engagementManifest = it.data
                        )
                    }
                    is Result.Error -> {
                        Log.e(CONVERSATION, "Error while fetching engagement manifest", it.error)
                    }
                }
            }
        }
    }
}