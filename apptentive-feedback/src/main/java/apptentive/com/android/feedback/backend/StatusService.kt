package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.SDKStatus
import apptentive.com.android.util.Result

internal interface StatusService {
    fun fetchStatus(
        conversationToken: String,
        applicationId: String,
        callback: (Result<SDKStatus>) -> Unit
    )
}
