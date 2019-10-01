package apptentive.com.android.feedback

import apptentive.com.android.util.generateUUID
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class GenerateUUIDRule : TestWatcher() {
    override fun starting(description: Description?) {
        mockkStatic("apptentive.com.android.util.UUIDUtils")
        every { generateUUID() } returns "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    }

    override fun finished(description: Description?) {
        // TODO: clear mocks
    }
}
