package apptentive.com.android.feedback

import apptentive.com.android.TestCase
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.utils.convertToGroupDate
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest : TestCase() {

    // This test will fail first 14 days of the year since it expects 14 days in the past to be in the same year.
    // This is to have greater test coverage of different days.
    // It's okay to ignore this test in that time frame.
    @Test
    fun testConvertToGroupDate() {
        val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

        // Within same week & year
        // Sunday, May 1 - Wednesday, September 11 == 13 - 23 characters
        val sameWeekAndYear = 13..23
        val sameWeekAndYearDays = 0..6 // Will fail first 6 days of the year.
        sameWeekAndYearDays.forEach {
            assertTrue(convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * it))).length in sameWeekAndYear)
        }

        // Outside of current week. Within same year.
        // May 1 - September 11 == 5 - 12 characters
        val outsideWeekSameYear = 5..12
        val outsideWeekSameYearDays = 7..14 // Will fail first 14 days of the year.
        outsideWeekSameYearDays.forEach {
            assertTrue(convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * it))).length in outsideWeekSameYear)
        }

        // Outside of current year.
        // May 1, 2021 - September 11, 2021 == 11 - 18 characters
        val outsideYear = 11..18
        val outsideYearDays = 365..730
        outsideYearDays.forEach {
            assertTrue(convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * it))).length in outsideYear)
        }
    }
}
