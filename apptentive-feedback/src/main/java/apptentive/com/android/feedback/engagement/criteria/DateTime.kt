package apptentive.com.android.feedback.engagement.criteria

// FIXME: unit testing
data class DateTime(val seconds: Long) : Comparable<DateTime> {
    override fun compareTo(other: DateTime) = seconds.compareTo(other.seconds)

    override fun toString() = seconds.toString()

    companion object {
        fun now(): DateTime {
            TODO()
        }
    }
}