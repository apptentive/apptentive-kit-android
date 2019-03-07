package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.Conversation
import io.reactivex.Observable

interface BackendService : ConversationService {
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
    //region ConversationService

    override fun fetchConversation(): Observable<Conversation> {
        TODO("not implemented")
    }

    //endregion
}
