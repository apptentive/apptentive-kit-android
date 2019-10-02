package apptentive.com.android.feedback.conversation

import apptentive.com.android.feedback.model.*
import apptentive.com.android.util.Factory
import apptentive.com.android.util.generateUUID

interface ConversationRepository {
    fun createConversation(): Conversation

    @Throws(ConversationSerializationException::class)
    fun saveConversation(conversation: Conversation)

    @Throws(ConversationSerializationException::class)
    fun loadConversation(): Conversation?
}

class DefaultConversationRepository(
    private val conversationSerializer: ConversationSerializer,
    private val appReleaseFactory: Factory<AppRelease>,
    private val personFactory: Factory<Person>,
    private val deviceFactory: Factory<Device>,
    private val sdkFactory: Factory<SDK>,
    private val manifestFactory: Factory<EngagementManifest>
) : ConversationRepository {
    override fun createConversation(): Conversation {
        return Conversation(
            localIdentifier = generateUUID(),
            person = personFactory.create(),
            device = deviceFactory.create(),
            appRelease = appReleaseFactory.create(),
            sdk = sdkFactory.create(),
            engagementManifest = manifestFactory.create()
        )
    }

    @Throws(ConversationSerializationException::class)
    override fun saveConversation(conversation: Conversation) =
        conversationSerializer.saveConversation(conversation)

    @Throws(ConversationSerializationException::class)
    override fun loadConversation(): Conversation? = conversationSerializer.loadConversation()
}