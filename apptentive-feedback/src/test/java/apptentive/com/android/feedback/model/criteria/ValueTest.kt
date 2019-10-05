package apptentive.com.android.feedback.model.criteria

import org.junit.Assert.assertTrue
import org.junit.Test

class ValueTest {
    @Test
    fun testString() {
        val value1: Value = Value.Str(description = "description", value = "value")
        val value2: Value = Value.Str(description = "description", value = "value")
        val value3: Value = Value.Str(description = "description", value = "VALUE")
        val value4: Value = Value.Str(description = "description", value = "another value")

        assertTrue(value1 == value2)
        assertTrue(value1 == value3)
        assertTrue(value1 != value4)
    }
}