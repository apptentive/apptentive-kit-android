package apptentive.com.android.feedback.conversation

data class ConversationRoster(
    var activeConversation: ConversationMetaData? = null,
    var loggedOut: List<ConversationMetaData> = listOf()
)
