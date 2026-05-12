package apptentive.com.android.feedback

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.core.getUtcOffset
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockTimeRule(
    private val currentTime: TimeInterval,
    private val utcOffset: Int
) : TestWatcher() {
    override fun starting(description: Description?) {
        mockkStatic(UTIL_CLASS)
        every { getTimeSeconds() } returns currentTime
        every { getUtcOffset() } returns utcOffset
    }

    override fun finished(description: Description?) {
        unmockkStatic(UTIL_CLASS)
    }

    companion object {
        private const val UTIL_CLASS = "apptentive.com.android.core.TypeAliasesKt"
    }
}
