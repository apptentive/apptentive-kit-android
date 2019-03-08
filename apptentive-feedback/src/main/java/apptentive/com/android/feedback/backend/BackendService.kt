package apptentive.com.android.feedback.backend

import apptentive.com.android.concurrent.Promise

interface BackendService : ConversationFetchService {
}

fun createBackendService(
    apptentiveKey: String,
    apptentiveSignature: String
): BackendService {
    return DefaultBackendService(apptentiveKey, apptentiveSignature)
}

private class DefaultBackendService(
    private val apptentiveKey: String,
    private val apptentiveSignature: String
) : BackendService {

    //region ConversationFetchService

    override fun fetchConversation(): Promise<ConversationCredentials> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //endregion
}
