package apptentive.com.android.feedback.backend

import apptentive.com.android.core.util.Result
import apptentive.com.android.feedback.model.SDKStatus

internal interface StatusService {
    fun fetchStatus(
        conversationToken: String,
        applicationId: String,
        callback: (Result<SDKStatus>) -> Unit
    )
}
