package apptentive.com.android.feedback.model

import apptentive.com.android.TestCase
import apptentive.com.android.debug.Assert.assertFalse
import apptentive.com.android.debug.Assert.assertTrue
import apptentive.com.android.feedback.mockDevice
import apptentive.com.android.feedback.mockEventPayload
import apptentive.com.android.feedback.mockPerson
import apptentive.com.android.feedback.mockSdk
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.feedback.utils.SensitiveDataUtils.toSnakeCase
import org.junit.Assert.assertEquals
import org.junit.Test

class SensitiveDataUtilsTest : TestCase() {

    @Test
    fun testIfSensitiveDataIsRedacted() {
        SensitiveDataUtils.shouldSanitizeLogMessages = true
        assertTrue(mockEventPayload.toString().contains("\"custom_data\":\"<REDACTED>\""))
        assertTrue(mockSdk.toString().contains("\"author_email\":\"<REDACTED>\""))
        assertTrue(mockSdk.toString().contains("\"author_name\":\"<REDACTED>\""))
        assertTrue(mockPerson.toString().contains("\"custom_data\":\"<REDACTED>\""))
        assertTrue(mockPerson.toString().contains("\"m_particle_id\":\"<REDACTED>\""))
        assertTrue(mockDevice.toString().contains("\"advertiser_id\":\"<REDACTED>\""))
        assertTrue(mockDevice.toString().contains("\"custom_data\":\"<REDACTED>\""))
    }

    @Test
    fun testSensitiveDataIsNotRedactedWhenTheFlagIsNotSet() {
        SensitiveDataUtils.shouldSanitizeLogMessages = false
        assertFalse(mockEventPayload.toString().contains("\"customData\":\"<REDACTED>\""))
        assertFalse(mockSdk.toString().contains("\"authorEmail\":\"<REDACTED>\",\"authorName\":\"<REDACTED>\","))
        assertFalse(mockPerson.toString().contains("\"customData\":\"<REDACTED>\""))
        assertFalse(mockPerson.toString().contains("\"mParticleId\":\"<REDACTED>\""))
        assertFalse(mockDevice.toString().contains("\"advertiserId\":\"<REDACTED>\""))
        assertFalse(mockDevice.toString().contains("\"customData\":\"<REDACTED>\""))
    }

    @Test
    fun `toSnakeCase converts camelCase to snake_case`() {
        val inputs = listOf(
            "camelCase",
            "doubleCamelCase",
            "mParticleId",
            "ABC",
            "lowercasetest"
        )
        val expected = listOf(
            "camel_case",
            "double_camel_case",
            "m_particle_id",
            "a_b_c",
            "lowercasetest"
        )

        inputs.forEachIndexed { index, input ->
            assertEquals(expected[index], input.toSnakeCase())
        }
    }
}
