package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.util.Factory

internal class DefaultEngagementManifestFactory : Factory<EngagementManifest> {
    override fun create() = EngagementManifest()
}
