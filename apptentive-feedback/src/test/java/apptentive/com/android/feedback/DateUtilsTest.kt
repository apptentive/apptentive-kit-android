package apptentive.com.android.feedback

import apptentive.com.android.TestCase
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.utils.convertToGroupDate
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest : TestCase() {

    @Test
    fun testConvertToGroupDate() {
        val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

        // Within same week & year
        // Sunday 1/1 - Wednesday 11/11 == 10 - 15 characters
        val sameWeekAndYear = 10..15
        assertTrue(convertToGroupDate(toSeconds(System.currentTimeMillis())).length in sameWeekAndYear)

        // Outside of current week. Within same year.
        // 1/1 - 11/11 == 3 - 5 characters
        val outsideWeekSameYear = 3..5
        val lastWeek = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 7))
        assertTrue(convertToGroupDate(lastWeek).length in outsideWeekSameYear)

        // Outside of current year.
        // 1/1/2021 - 11/11/2021 == 8 - 10 characters
        val outsideYear = 8..10
        val lastYear = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 365))
        assertTrue(convertToGroupDate(lastYear).length in outsideYear)
    }
}
