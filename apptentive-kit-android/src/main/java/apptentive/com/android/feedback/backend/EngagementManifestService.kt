package apptentive.com.android.feedback.backend

import apptentive.com.android.core.network.Result
import apptentive.com.android.feedback.model.EngagementManifest

internal interface EngagementManifestService {
    fun fetchEngagementManifest(
        conversationToken: String,
        conversationId: String,
        callback: (Result<EngagementManifest>) -> Unit
    )
}
