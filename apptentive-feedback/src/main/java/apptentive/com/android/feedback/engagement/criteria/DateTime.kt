package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.getTimeSeconds

// FIXME: unit testing
data class DateTime(val seconds: TimeInterval) : Comparable<DateTime> {
    override fun compareTo(other: DateTime) = seconds.compareTo(other.seconds)

    override fun toString() = seconds.toString()

    companion object {
        fun now(): DateTime {
            val seconds = getTimeSeconds()
            return DateTime(seconds)
        }
    }
}