package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.util.Factory

internal class DefaultEngagementDataFactory : Factory<EngagementData> {
    override fun create() = EngagementData()
}
