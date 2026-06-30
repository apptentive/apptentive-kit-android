package apptentive.com.android.feedback.platform

import apptentive.com.android.core.Factory
import apptentive.com.android.feedback.model.EngagementManifest

internal class DefaultEngagementManifestFactory : Factory<EngagementManifest> {
    override fun create() = EngagementManifest()
}
