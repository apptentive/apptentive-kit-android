package apptentive.com.android.util

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.TimeSource

class MockTimeSource(var time: TimeInterval) : TimeSource {
    override fun getTimeSeconds() = time
}