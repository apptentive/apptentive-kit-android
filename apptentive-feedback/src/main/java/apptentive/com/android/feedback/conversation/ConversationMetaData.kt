package apptentive.com.android.feedback.conversation

import apptentive.com.android.encryption.EncryptionKey

data class ConversationMetaData(var state: ConversationState, var path: String)

sealed class ConversationState {
    // State of a activeConversation when ConversationRoster is created
    // when the app register for the first time
    object Undefined : ConversationState()

    // TODO figure out if this is needed
    object LegacyPending : ConversationState()

    // Conversation is created but the token is not yet fetched
    object AnonymousPending : ConversationState()

    // Conversation is created and token is fetched
    data class Anonymous(val id: String, val conversationToken: String) : ConversationState() // TODO id & token is needed?

    // Conversation is logged in
    data class LoggedIn(val id: String, val conversationToken: String, val subject: String, val encryptionKey: EncryptionKey) : ConversationState() // TODO id & token is needed? // encryptionkey should be key alias

    // Conversation is logged out
    data class LoggedOut(val id: String, val subject: String) : ConversationState()
}
