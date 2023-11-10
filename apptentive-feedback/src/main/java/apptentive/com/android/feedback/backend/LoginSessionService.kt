package apptentive.com.android.feedback.backend

import apptentive.com.android.util.Result

internal interface LoginSessionService {
    fun loginSession(
        conversationId: String,
        jwtToken: String,
        callback: (Result<ConversationFetchResponse>) -> Unit
    )
}

internal data class LoginSessionResponse(
    val encryptionKey: String
)

internal data class LoginSessionRequest(
    val token: String // JWT token
)
