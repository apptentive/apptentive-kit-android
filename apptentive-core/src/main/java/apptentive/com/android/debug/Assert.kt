package apptentive.com.android.debug

import java.lang.AssertionError

object Assert {
    fun assertTrue(condition: Boolean, message: String) {
        if (!condition) {
            fail(message)
        }
    }

    fun assertFalse(condition: Boolean, message: String) {
        if (condition) {
            fail(message)
        }
    }
    fun assertEqual(a: Any?, b: Any?, message: String) {
        if (a != b) {
            fail(message)
        }
    }

    private fun fail(message: String) {
        throw AssertionError(message) // FIXME: add assertion implementation
    }
}