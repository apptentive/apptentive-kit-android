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
    data class Anonymous(val key: String, val signature: String) : ConversationState() // TODO key & signature is needed?

    // Conversation is logged in
    data class LoggedIn(val key: String, val signature: String, val subject: String, val encryptionKey: EncryptionKey) : ConversationState() // TODO key & signature is needed?

    // Conversation is logged out
    data class LoggedOut(val id: String, val subject: String) : ConversationState()
}
