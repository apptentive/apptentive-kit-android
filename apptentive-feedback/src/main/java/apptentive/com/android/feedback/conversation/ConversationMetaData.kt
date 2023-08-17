package apptentive.com.android.feedback.conversation

import apptentive.com.android.encryption.EncryptionKey

data class ConversationMetaData(var state: ConversationState, var path: String)

sealed class ConversationState {
    // State of a activeConversation when ConversationRoster is created
    // when the app register for the first time
    object Undefined : ConversationState()

    // Conversation is created but the token is not yet fetched
    object AnonymousPending : ConversationState()

    // Conversation is created and token is fetched
    object Anonymous : ConversationState()

    // Conversation is logged in
    data class LoggedIn(val subject: String, val encryptionKey: EncryptionKey) : ConversationState() // TODO encryptionkey should be key alias

    // Conversation is logged out
    data class LoggedOut(val id: String, val subject: String) : ConversationState()
}
