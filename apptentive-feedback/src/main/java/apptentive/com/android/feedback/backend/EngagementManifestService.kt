package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.util.Result

internal interface EngagementManifestService {
    fun fetchEngagementManifest(
        conversationToken: String,
        conversationId: String,
        callback: (Result<EngagementManifest>) -> Unit
    )
}
