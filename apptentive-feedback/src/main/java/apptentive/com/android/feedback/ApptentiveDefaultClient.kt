package apptentive.com.android.feedback

import android.content.Context
import apptentive.com.android.feedback.backend.BackendService
import apptentive.com.android.feedback.backend.createBackendService

internal class ApptentiveDefaultClient(
    apptentiveKey: String,
    apptentiveSignature: String
) : ApptentiveClient {
    private val backendService: BackendService = createBackendService(apptentiveKey, apptentiveSignature)

    internal fun start() {
    }

    override fun engage(context: Context, event: String) {
        TODO("Implement me")
    }
}