package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.SDK

interface ConversationFetchService {
    suspend fun fetchConversationToken(request: ConversationTokenFetchRequest): ConversationTokenFetchResponse
}

data class ConversationTokenFetchRequest(
    val device: Device,
    val sdk: SDK,
    val appRelease: AppRelease
)

data class ConversationTokenFetchResponse(
    val device: Device,
    val sdk: SDK,
    val appRelease: AppRelease
)