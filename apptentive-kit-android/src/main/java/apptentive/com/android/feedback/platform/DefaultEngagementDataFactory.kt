package apptentive.com.android.feedback.platform

import apptentive.com.android.core.util.Factory
import apptentive.com.android.feedback.model.EngagementData

internal class DefaultEngagementDataFactory : Factory<EngagementData> {
    override fun create() = EngagementData()
}
