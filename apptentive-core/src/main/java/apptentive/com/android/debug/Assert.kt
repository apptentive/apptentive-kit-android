package apptentive.com.android.debug

import java.lang.AssertionError

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
        throw AssertionError(message) // FIXME: add assertion implementation
    }
}
