package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.util.Factory

internal class DefaultSDKFactory(
    private val version: String,
    private val distribution: String,
    private val distributionVersion: String
) : Factory<SDK> {
    override fun create(): SDK {
        return SDK(
            version = version,
            platform = "Android",
            distribution = distribution,
            distributionVersion = distributionVersion
        )
    }
}
