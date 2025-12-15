package apptentive.com.android.feedback.utils

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Logger
import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.feedback.MockAndroidLoggerProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StringUtilsTest {

    @Before
    fun setUp() {
        DependencyProvider.register<Logger>(MockAndroidLoggerProvider())
    }

    @Test
    fun testShouldRefreshManifest_emptyLastFetchTime_returnsTrue() {
        assertTrue(shouldRefreshManifest("", 1234.0))
    }

    @Test
    fun testShouldRefreshManifest_lastUpdateZero_returnsFalse() {
        assertFalse(shouldRefreshManifest("1234.0", 0.0,))
    }

    @Test
    fun testShouldRefreshManifest_equalTimestamps_returnsFalse() {
        val timestamp = 5678.0
        assertFalse(shouldRefreshManifest(timestamp.toString(), timestamp))
    }

    @Test
    fun testShouldRefreshManifest_lastFetchOlder_returnsTrue() {
        val lastFetch = 1.76245335541E9
        val lastUpdate = 1762448101283.0
        assertTrue(shouldRefreshManifest(lastFetch.toString(), lastUpdate))
    }

    @Test
    fun testShouldRefreshManifest_lastFetchNewer_returnsFalse() {
        val lastFetch = 3000.0
        val lastUpdate = 2000.0
        assertFalse(shouldRefreshManifest(lastFetch.toString(), lastUpdate))
    }

    @Test
    fun getTimeAsDouble_validDoubleString_returnsDoubleValue() {
        val result = getTimeAsDouble("1234.56")
        assertTrue(result == 1234.56)
    }

    @Test
    fun getTimeAsDouble_integerString_returnsDoubleValue() {
        val result = getTimeAsDouble("42")
        assertTrue(result == 42.0)
    }

    @Test
    fun getTimeAsDouble_emptyString_returnsZero() {
        val result = getTimeAsDouble("")
        assertTrue(result == 0.0)
    }

    @Test
    fun getTimeAsDouble_invalidString_returnsZero() {
        val result = getTimeAsDouble("not_a_number")
        assertTrue(result == 0.0)
    }

    @Test
    fun getTimeAsDouble_negativeNumberString_returnsNegativeDouble() {
        val result = getTimeAsDouble("-987.65")
        assertTrue(result == -987.65)
    }

    @Test
    fun testHasItBeenAnHour() {
        val now = getTimeSeconds()
        val oneHourAgo = now - 3601 // just over an hour ago
        val justUnderHourAgo = now - 3599 // just under an hour ago

        assertTrue(hasItBeenAnHour(oneHourAgo.toString()))
        assertFalse(hasItBeenAnHour(justUnderHourAgo.toString()))
        assertFalse(hasItBeenAnHour(now.toString())) // exactly now
    }

    @Test
    fun testNullOrBlankVersion_returnsTrue() {
        assertTrue(isVersionLessThan610(null))
        assertTrue(isVersionLessThan610(""))
        assertTrue(isVersionLessThan610("   "))
    }

    @Test
    fun testVersionLessThan610_returnsTrue() {
        assertTrue(isVersionLessThan610("5.9.9"))
        assertTrue(isVersionLessThan610("6.9.9"))
        assertTrue(isVersionLessThan610("6.0.0"))
        assertTrue(isVersionLessThan610("6.9"))
        assertTrue(isVersionLessThan610("6.9.0"))
        assertTrue(isVersionLessThan610("6.9.9"))
    }

    @Test
    fun testVersionEqualTo610_returnsFalse() {
        assertFalse(isVersionLessThan610("6.10.0"))
    }

    @Test
    fun testVersionGreaterThan610_returnsFalse() {
        assertFalse(isVersionLessThan610("6.10.1"))
        assertFalse(isVersionLessThan610("6.11.0"))
        assertFalse(isVersionLessThan610("7.0.0"))
        assertFalse(isVersionLessThan610("10.0.0"))
    }
}
