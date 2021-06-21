package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.util.Factory

class DefaultEngagementDataFactory : Factory<EngagementData> {
    override fun create() = EngagementData()
}
