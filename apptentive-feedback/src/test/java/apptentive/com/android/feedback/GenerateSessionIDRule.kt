package apptentive.com.android

import apptentive.com.android.feedback.ApptentiveDefaultClient
import apptentive.com.android.feedback.ApptentiveDefaultClient.Companion.getSessionId
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class GenerateSessionIDRule : TestWatcher() {
    override fun starting(description: Description) {
        mockkObject(ApptentiveDefaultClient) {
            every {
                getSessionId()
            } returns "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
        }
    }

    override fun finished(description: Description) {
        unmockkObject(ApptentiveDefaultClient)
    }

    companion object {
        private const val UTIL_CLASS = "apptentive.com.android.util.UUIDUtils"
    }
}
