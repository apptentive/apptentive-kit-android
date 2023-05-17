package apptentive.com.android

import apptentive.com.android.util.generateUUID
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class GenerateUUIDRule : TestWatcher() {
    override fun starting(description: Description) {
        mockkStatic(UTIL_CLASS)
        every { generateUUID() } returns "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    }

    override fun finished(description: Description) {
        unmockkStatic(UTIL_CLASS)
    }

    companion object {
        private const val UTIL_CLASS = "apptentive.com.android.util.UUIDUtils"
    }
}
