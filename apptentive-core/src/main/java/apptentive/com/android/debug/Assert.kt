package apptentive.com.android.debug

import androidx.annotation.VisibleForTesting
import java.lang.AssertionError

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
object Assert {
    fun assertTrue(condition: Boolean, message: String? = null) {
        assertEqual(true, condition, message)
    }

    fun assertFalse(condition: Boolean, message: String? = null) {
        assertEqual(false, condition, message)
    }
    fun assertEqual(a: Any?, b: Any?, message: String? = null) {
        if (a != b) {
            fail(message ?: "Expected $a but was $b")
        }
    }

    private fun fail(message: String) {
        throw AssertionError(message)
    }
}
