package apptentive.com.android.feedback

import apptentive.com.android.util.generateUUID
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class GenerateUUIDRule : TestRule {
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            try {
                mockkStatic("apptentive.com.android.util.UUIDUtils")
                every { generateUUID() } returns "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                base.evaluate()
            } finally {

            }
        }
    }
}
