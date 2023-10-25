package apptentive.com.android.feedback.conversation

import apptentive.com.android.feedback.model.SensitiveDataKey

data class ConversationMetaData(var state: ConversationState, var path: String)

sealed class ConversationState {
    // State of a activeConversation when ConversationRoster is created
    // when the app register for the first time
    object Undefined : ConversationState()

    // Conversation is created but the token is not yet fetched
    object AnonymousPending : ConversationState()

    // Conversation is created and token is fetched
    object Anonymous : ConversationState()

    // null Conversation state - used for serialization
    object Null : ConversationState()

    // Conversation is logged in
    data class LoggedIn(val subject: String, @SensitiveDataKey val encryptionWrapperBytes: ByteArray) : ConversationState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LoggedIn

            if (subject != other.subject) return false
            if (!encryptionWrapperBytes.contentEquals(other.encryptionWrapperBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = subject.hashCode()
            result = 31 * result + encryptionWrapperBytes.contentHashCode()
            return result
        }
    }

    // Conversation is logged out
    data class LoggedOut(val id: String, val subject: String) : ConversationState()
}
