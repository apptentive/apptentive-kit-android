package apptentive.com.android.feedback.platform

import apptentive.com.android.core.Factory
import apptentive.com.android.feedback.model.EngagementData

internal class DefaultEngagementDataFactory : Factory<EngagementData> {
    override fun create() = EngagementData()
}
