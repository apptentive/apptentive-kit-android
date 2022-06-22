package apptentive.com.android.feedback.message

import apptentive.com.android.feedback.model.Conversation

internal interface ConversationListener {
    fun onConversationChanged(conversation: Conversation)
}
