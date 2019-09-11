package apptentive.com.android.feedback.backend

import apptentive.com.android.concurrent.Promise

interface BackendService : ConversationFetchService {
}

fun createBackendService(
    apptentiveKey: String,
    apptentiveSignature: String
): BackendService {
    TODO()
}
